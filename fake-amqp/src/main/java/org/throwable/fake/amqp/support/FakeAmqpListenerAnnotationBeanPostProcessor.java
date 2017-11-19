package org.throwable.fake.amqp.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.MultiMethodRabbitListenerEndpoint;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.throwable.fake.amqp.annotation.FakeAmqpHandler;
import org.throwable.fake.amqp.annotation.FakeAmqpListener;
import org.throwable.fake.amqp.annotation.FakeAmqpListeners;
import org.throwable.fake.amqp.common.AmqpConstant;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 18:50
 */
@Slf4j
public class FakeAmqpListenerAnnotationBeanPostProcessor
		implements BeanFactoryAware, BeanClassLoaderAware, BeanPostProcessor, EnvironmentAware,
		Ordered, SmartInitializingSingleton {

	private static final String DEFAULT_RABBIT_ADMIN_BEAN_NAME = "rabbitAdmin";
	public static final String EMPTY_STRING_ARGUMENTS_PROPERTY = "fake.amqp.emptyStringArguments";
	private static final String DEFAULT_GROUP = "fakeListenerEndpoints";
	private static final AmqpMessageConverterFactory CONVERTER_FACTORY = new ContentTypeDelegatingConverterFactory();
	private static final ContentTypeDelegatingMessageConverter CONVERTER;
	private DefaultListableBeanFactory beanFactory;
	private ClassLoader beanClassLoader;
	private BeanExpressionResolver resolver = new StandardBeanExpressionResolver();
	private BeanExpressionContext expressionContext;
	private final Set<String> emptyStringArguments = new HashSet<>();
	private ConversionService conversionService = new DefaultConversionService();
	private final AmqpHandlerMethodFactoryAdapter methodFactoryAdapter = new AmqpHandlerMethodFactoryAdapter();
	private final FakeAmqpListenerEndpointRegistrar registrar = new FakeAmqpListenerEndpointRegistrar();
	private final ConcurrentMap<Class<?>, TypeMetadata> typeCache = new ConcurrentHashMap<>();
	private FakeAmqpListenerEndpointRegistry endpointRegistry;
	private final AtomicInteger counter = new AtomicInteger();

	static {
		CONVERTER = (ContentTypeDelegatingMessageConverter) CONVERTER_FACTORY.createMessageConverter();
		CONVERTER.addDelegate(MediaType.APPLICATION_JSON_VALUE, new Jackson2JsonMessageConverter());
		CONVERTER.addDelegate(MediaType.APPLICATION_JSON_UTF8_VALUE, new Jackson2JsonMessageConverter());
	}

	public FakeAmqpListenerAnnotationBeanPostProcessor() {
		this.emptyStringArguments.add("x-dead-letter-exchange");
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
		this.resolver = this.beanFactory.getBeanExpressionResolver();
		this.expressionContext = new BeanExpressionContext(this.beanFactory, null);
		prepareEndpointRegistrar();
	}

	@Override
	public void setEnvironment(Environment environment) {
		String property = environment.getProperty(EMPTY_STRING_ARGUMENTS_PROPERTY, String.class);
		if (null != property) {
			this.emptyStringArguments.addAll(StringUtils.commaDelimitedListToSet(property));
		}
	}

	public FakeAmqpListenerEndpointRegistry getEndpointRegistry() {
		return endpointRegistry;
	}

	public void setEndpointRegistry(FakeAmqpListenerEndpointRegistry endpointRegistry) {
		this.endpointRegistry = endpointRegistry;
	}

	@Override
	public void afterSingletonsInstantiated() {
		this.registrar.afterPropertiesSet();
		this.typeCache.clear();
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		TypeMetadata typeMetadata = typeCache.get(targetClass);
		if (null == typeMetadata) {
			typeMetadata = buildListenersAnnotationMetadata(targetClass);
			typeCache.putIfAbsent(targetClass, typeMetadata);
		}
		for (ListenerMethod listenerMethod : typeMetadata.listenerMethods) {
			for (FakeAmqpListener fakeAmqpListener : listenerMethod.annotations) {
				processAmqpListeners(fakeAmqpListener, listenerMethod.method, bean, targetClass, beanName);
			}
		}
		if (typeMetadata.handlerMethods.length > 0) {
			processMultiHandlerMethodListeners(typeMetadata.classAnnotations, typeMetadata.handlerMethods, bean,
					targetClass, beanName);
		}
		return bean;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	private void prepareEndpointRegistrar() {
		Assert.state(null != this.beanFactory, "BeanFactory must be set to find endpoint registry by bean name");
		this.registrar.setBeanFactory(this.beanFactory);
		Map<String, FakeAmqpListenerConfigurer> configurerMap = this.beanFactory.getBeansOfType(FakeAmqpListenerConfigurer.class);
		if (null != configurerMap && !configurerMap.isEmpty()) {
			for (FakeAmqpListenerConfigurer configurer : configurerMap.values()) {
				configurer.configureRabbitListeners(this.registrar);
			}
		}
		if (null == this.registrar.getEndpointRegistry()) {
			this.endpointRegistry = this.beanFactory.getBean(AmqpConstant.AMQP_ENDPOINT_REGISTRY_BEAN_NAME,
					FakeAmqpListenerEndpointRegistry.class);
			this.registrar.setEndpointRegistry(this.endpointRegistry);
		}
		MessageHandlerMethodFactory handlerMethodFactory = this.registrar.getMessageHandlerMethodFactory();
		if (handlerMethodFactory != null) {
			this.methodFactoryAdapter.setMessageHandlerMethodFactory(handlerMethodFactory);
		}
	}

	private void processAmqpListeners(FakeAmqpListener fakeAmqpListener,
									  Method method,
									  Object bean,
									  Class<?> targetClass,
									  String beanName) {
		Method methodToUse = checkMethodProxyAndReturn(method, bean);
		MethodRabbitListenerEndpoint endpoint = new MethodRabbitListenerEndpoint();
		endpoint.setMethod(methodToUse);
		endpoint.setBeanFactory(this.beanFactory);
		processListener(endpoint, fakeAmqpListener, bean, targetClass, beanName);
	}

	private void processMultiHandlerMethodListeners(FakeAmqpListener[] classLevelListeners,
													Method[] multiMethods,
													Object bean,
													Class<?> targetClass,
													String beanName) {
		List<Method> checkedMethods = new ArrayList<>();
		for (Method method : multiMethods) {
			checkedMethods.add(checkMethodProxyAndReturn(method, bean));
		}
		for (FakeAmqpListener classLevelListener : classLevelListeners) {
			MultiMethodRabbitListenerEndpoint endpoint = new MultiMethodRabbitListenerEndpoint(checkedMethods, bean);
			endpoint.setBeanFactory(this.beanFactory);
			processListener(endpoint, classLevelListener, bean, targetClass, beanName);
		}
	}

	private void processListener(MethodRabbitListenerEndpoint endpoint,
								 FakeAmqpListener fakeAmqpListener,
								 Object bean,
								 Class<?> targetClass,
								 String beanName) {
		RabbitAdmin rabbitAdmin = getRabbitAdminBeanFromContext(fakeAmqpListener, targetClass);
		endpoint.setBean(bean);
		endpoint.setId(resolveEndpointId(fakeAmqpListener));
		endpoint.setMessageHandlerMethodFactory(this.methodFactoryAdapter);
		endpoint.setQueueNames(resolveQueues(fakeAmqpListener, rabbitAdmin));
		endpoint.setGroup(resolveEndpointGroup(fakeAmqpListener));
		endpoint.setExclusive(fakeAmqpListener.exclusive());
		Integer priority = resolvePriority(fakeAmqpListener);
		if (null != priority) {
			endpoint.setPriority(priority);
		}
		endpoint.setAdmin(rabbitAdmin);
		String containerFactoryBeanName = getOrCreateRabbitListenerContainerFactory(targetClass, beanName);
		EndpointDescriptorMetadata metadata = new EndpointDescriptorMetadata(containerFactoryBeanName,
				fakeAmqpListener.acknowledgeMode(), fakeAmqpListener.concurrentConsumers(), fakeAmqpListener.maxConcurrentConsumers());
		this.registrar.registerEndpoint(endpoint, metadata);
	}

	private TypeMetadata buildListenersAnnotationMetadata(Class<?> targetClass) {
		Collection<FakeAmqpListener> classLevelListeners = findListenerAnnotations(targetClass);
		final boolean hasClassLevelListeners = !classLevelListeners.isEmpty();
		final List<ListenerMethod> methods = new ArrayList<>();
		final List<Method> multiMethods = new ArrayList<>();
		ReflectionUtils.doWithMethods(targetClass, method -> {
			Collection<FakeAmqpListener> methodLevelListeners = FakeAmqpListenerAnnotationBeanPostProcessor.this.findListenerAnnotations(method);
			if (null != methodLevelListeners && !methodLevelListeners.isEmpty()) {
				methods.add(new ListenerMethod(method, methodLevelListeners.toArray(new FakeAmqpListener[methodLevelListeners.size()])));
			}
			if (hasClassLevelListeners) {
				FakeAmqpHandler fakeAmqpHandler = AnnotationUtils.findAnnotation(method, FakeAmqpHandler.class);
				if (null != fakeAmqpHandler) {
					multiMethods.add(method);
				}
			}
		}, ReflectionUtils.USER_DECLARED_METHODS);
		if (methods.isEmpty() && multiMethods.isEmpty()) {
			return TypeMetadata.EMPTY;
		}
		return new TypeMetadata(methods.toArray(new ListenerMethod[methods.size()]),
				multiMethods.toArray(new Method[multiMethods.size()]),
				classLevelListeners.toArray(new FakeAmqpListener[classLevelListeners.size()]));
	}

	private Collection<FakeAmqpListener> findListenerAnnotations(Class<?> targetClass) {
		Set<FakeAmqpListener> listeners = new HashSet<>();
		FakeAmqpListener annotation = AnnotationUtils.findAnnotation(targetClass, FakeAmqpListener.class);
		if (null != annotation) {
			listeners.add(annotation);
		}
		FakeAmqpListeners annotations = AnnotationUtils.findAnnotation(targetClass, FakeAmqpListeners.class);
		if (null != annotations) {
			Collections.addAll(listeners, annotations.value());
		}
		return listeners;
	}

	private Collection<FakeAmqpListener> findListenerAnnotations(Method method) {
		Set<FakeAmqpListener> listeners = new HashSet<>();
		FakeAmqpListener annotation = AnnotationUtils.findAnnotation(method, FakeAmqpListener.class);
		if (null != annotation) {
			listeners.add(annotation);
		}
		FakeAmqpListeners annotations = AnnotationUtils.findAnnotation(method, FakeAmqpListeners.class);
		if (null != annotations) {
			Collections.addAll(listeners, annotations.value());
		}
		return listeners;
	}

	private String resolveRabbitAdminBeanName(FakeAmqpListener fakeAmqpListener) {
		String rabbitAdminBeanName = resolve(fakeAmqpListener.rabbitAdmin());
		if (!StringUtils.hasText(rabbitAdminBeanName)) {
			rabbitAdminBeanName = DEFAULT_RABBIT_ADMIN_BEAN_NAME;
		}
		return rabbitAdminBeanName;
	}

	private RabbitAdmin getRabbitAdminBeanFromContext(FakeAmqpListener fakeAmqpListener,
													  Class<?> targetClass) {
		String rabbitAdminBeanName = resolveRabbitAdminBeanName(fakeAmqpListener);
		try {
			return beanFactory.getBean(rabbitAdminBeanName, RabbitAdmin.class);
		} catch (NoSuchBeanDefinitionException ex) {
			throw new BeanInitializationException("Could not register rabbit listener endpoint on [" +
					targetClass + "], no " + RabbitAdmin.class.getSimpleName() + " with id '" +
					rabbitAdminBeanName + "' was found in the application context", ex);
		}
	}

	private String getOrCreateRabbitListenerContainerFactory(Class<?> targetClass,
															 String beanName) {
		String containerFactoryBeanName = AmqpConstant.AMQP_CONTAINER_FACTORY_BEAN_NAME;
		if (!beanFactory.containsBean(containerFactoryBeanName)) {
			if (log.isInfoEnabled()) {
				log.info("Create RabbitListenerContainerFactory for bean {}", beanName);
			}
			CachingConnectionFactory connectionFactory = getCachingConnectionFactoryFromContext(targetClass);
			SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer
					= beanFactory.getBean(SimpleRabbitListenerContainerFactoryConfigurer.class);
			SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
			factoryConfigurer.configure(containerFactory, connectionFactory);
			containerFactory.setConnectionFactory(connectionFactory);
			containerFactory.setAcknowledgeMode(AcknowledgeMode.NONE);  //default none
			containerFactory.setMessageConverter(CONVERTER);
			beanFactory.registerSingleton(containerFactoryBeanName, containerFactory);
		}
		return containerFactoryBeanName;
	}

	private CachingConnectionFactory getCachingConnectionFactoryFromContext(Class<?> targetClass) {
		try {
			return beanFactory.getBean(CachingConnectionFactory.class);
		} catch (NoSuchBeanDefinitionException ex) {
			throw new BeanInitializationException("Could not register rabbit listener endpoint on [" +
					targetClass + "], no " + CachingConnectionFactory.class.getSimpleName() +
					"was found in the application context", ex);
		}
	}

	private String resolveEndpointId(FakeAmqpListener fakeAmqpListener) {
		if (StringUtils.hasText(fakeAmqpListener.id())) {
			return resolve(fakeAmqpListener.id());
		} else {
			return "org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#" + this.counter.getAndIncrement();
		}
	}

	private Integer resolvePriority(FakeAmqpListener fakeAmqpListener) {
		String priority = resolve(fakeAmqpListener.priority());
		if (StringUtils.hasText(priority)) {
			try {
				return Integer.valueOf(priority);
			} catch (NumberFormatException ex) {
				throw new BeanInitializationException("Invalid priority value for " +
						fakeAmqpListener + " (must be an integer)", ex);
			}
		}
		return null;
	}

	private String resolveEndpointGroup(FakeAmqpListener fakeAmqpListener) {
		if (StringUtils.hasText(fakeAmqpListener.group())) {
			Object resolvedGroup = resolveExpression(fakeAmqpListener.group());
			if (resolvedGroup instanceof String) {
				return (String) resolvedGroup;
			}
		}
		return DEFAULT_GROUP;
	}

	private String[] resolveQueues(FakeAmqpListener fakeAmqpListener,
								   RabbitAdmin rabbitAdmin) {
		String[] queues = fakeAmqpListener.queues();
		QueueBinding[] bindings = fakeAmqpListener.bindings();
		if (queues.length > 0 && bindings.length > 0) {
			throw new BeanInitializationException("@FakeAmqpListener can have 'queues' or 'bindings' but not both");
		}
		List<String> result = new ArrayList<>();
		if (queues.length > 0) {
			for (String queue : queues) {
				Object resolvedValue = resolveExpression(queue);
				resolveAsString(resolvedValue, result);
			}
			if (fakeAmqpListener.autoDeclare()) {
				result.forEach(rabbitAdmin::deleteQueue);
			}
		} else {
			return registerBeansForDeclaration(fakeAmqpListener, rabbitAdmin);
		}
		return result.toArray(new String[result.size()]);
	}

	@SuppressWarnings("unchecked")
	private void resolveAsString(Object resolvedValue, List<String> result) {
		Object resolvedValueToUse = resolvedValue;
		if (resolvedValue instanceof String[]) {
			resolvedValueToUse = Arrays.asList((String[]) resolvedValue);
		}
		if (resolvedValueToUse instanceof Queue) {
			result.add(((Queue) resolvedValueToUse).getName());
		} else if (resolvedValueToUse instanceof String) {
			result.add((String) resolvedValueToUse);
		} else if (resolvedValueToUse instanceof Iterable) {
			for (Object object : (Iterable<Object>) resolvedValueToUse) {
				resolveAsString(object, result);
			}
		} else {
			throw new IllegalArgumentException(String.format(
					"@RabbitListener can't resolve '%s' as either a String or a Queue",
					resolvedValue));
		}
	}

	private String[] registerBeansForDeclaration(FakeAmqpListener fakeAmqpListener,
												 RabbitAdmin rabbitAdmin) {
		List<String> queues = new ArrayList<>();
		for (QueueBinding binding : fakeAmqpListener.bindings()) {
			String queueName = declareQueueForQueueBinding(binding, rabbitAdmin, fakeAmqpListener.autoDeclare());
			queues.add(queueName);
			declareExchangeAndBinding(binding, queueName, rabbitAdmin, fakeAmqpListener.autoDeclare());
		}
		return queues.toArray(new String[queues.size()]);
	}

	private String declareQueueForQueueBinding(QueueBinding binding,
											   RabbitAdmin rabbitAdmin,
											   boolean autoDeclare) {
		org.springframework.amqp.rabbit.annotation.Queue bindingQueue = binding.value();
		String queueName = (String) resolveExpression(bindingQueue.value());
		boolean exclusive = false;
		boolean autoDelete = false;
		if (!StringUtils.hasText(queueName)) {
			queueName = UUID.randomUUID().toString();
			if (!StringUtils.hasText(bindingQueue.exclusive())
					|| resolveExpressionAsBoolean(bindingQueue.exclusive())) {
				exclusive = true;
			}
			if (!StringUtils.hasText(bindingQueue.autoDelete())
					|| resolveExpressionAsBoolean(bindingQueue.autoDelete())) {
				autoDelete = true;
			}
		} else {
			exclusive = resolveExpressionAsBoolean(bindingQueue.exclusive());
			autoDelete = resolveExpressionAsBoolean(bindingQueue.autoDelete());
		}
		org.springframework.amqp.core.Queue queue = new org.springframework.amqp.core.Queue(queueName,
				resolveExpressionAsBoolean(bindingQueue.durable()),
				exclusive,
				autoDelete,
				resolveArguments(bindingQueue.arguments()));
		queue.setIgnoreDeclarationExceptions(resolveExpressionAsBoolean(bindingQueue.ignoreDeclarationExceptions()));
		if (autoDeclare) {
			rabbitAdmin.declareQueue(queue);
		}
		return queueName;
	}

	private void declareExchangeAndBinding(QueueBinding binding,
										   String queueName,
										   RabbitAdmin rabbitAdmin,
										   boolean autoDeclare) {
		org.springframework.amqp.rabbit.annotation.Exchange bindingExchange = binding.exchange();
		String exchangeName = resolveExpressionAsString(bindingExchange.value(), "@Exchange.exchange");
		String exchangeType = resolveExpressionAsString(bindingExchange.type(), "@Exchange.type");
		String routingKey = resolveExpressionAsString(binding.key(), "@QueueBinding.key");
		Exchange exchange;
		Binding actualBinding;
		if (exchangeType.equals(ExchangeTypes.DIRECT)) {
			exchange = directExchange(bindingExchange, exchangeName);
			actualBinding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey,
					resolveArguments(binding.arguments()));
		} else if (exchangeType.equals(ExchangeTypes.FANOUT)) {
			exchange = fanoutExchange(bindingExchange, exchangeName);
			actualBinding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "",
					resolveArguments(binding.arguments()));
		} else if (exchangeType.equals(ExchangeTypes.TOPIC)) {
			exchange = topicExchange(bindingExchange, exchangeName);
			actualBinding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey,
					resolveArguments(binding.arguments()));
		} else if (exchangeType.equals(ExchangeTypes.HEADERS)) {
			exchange = headersExchange(bindingExchange, exchangeName);
			actualBinding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey,
					resolveArguments(binding.arguments()));
		} else {
			throw new BeanInitializationException("Unexpected exchange type: " + exchangeType);
		}
		AbstractExchange abstractExchange = (AbstractExchange) exchange;
		abstractExchange.setInternal(resolveExpressionAsBoolean(bindingExchange.internal()));
		abstractExchange.setDelayed(resolveExpressionAsBoolean(bindingExchange.delayed()));
		abstractExchange.setIgnoreDeclarationExceptions(resolveExpressionAsBoolean(bindingExchange.ignoreDeclarationExceptions()));
		actualBinding.setIgnoreDeclarationExceptions(resolveExpressionAsBoolean(binding.ignoreDeclarationExceptions()));
		if (autoDeclare) {
			rabbitAdmin.declareExchange(abstractExchange);
			rabbitAdmin.declareBinding(actualBinding);
		}
	}

	private Exchange directExchange(org.springframework.amqp.rabbit.annotation.Exchange bindingExchange,
									String exchangeName) {
		return new DirectExchange(exchangeName,
				resolveExpressionAsBoolean(bindingExchange.durable()),
				resolveExpressionAsBoolean(bindingExchange.autoDelete()),
				resolveArguments(bindingExchange.arguments()));
	}

	private Exchange fanoutExchange(org.springframework.amqp.rabbit.annotation.Exchange bindingExchange,
									String exchangeName) {
		return new FanoutExchange(exchangeName,
				resolveExpressionAsBoolean(bindingExchange.durable()),
				resolveExpressionAsBoolean(bindingExchange.autoDelete()),
				resolveArguments(bindingExchange.arguments()));
	}

	private Exchange topicExchange(org.springframework.amqp.rabbit.annotation.Exchange bindingExchange,
								   String exchangeName) {
		return new TopicExchange(exchangeName,
				resolveExpressionAsBoolean(bindingExchange.durable()),
				resolveExpressionAsBoolean(bindingExchange.autoDelete()),
				resolveArguments(bindingExchange.arguments()));
	}

	private Exchange headersExchange(org.springframework.amqp.rabbit.annotation.Exchange bindingExchange,
									 String exchangeName) {
		return new HeadersExchange(exchangeName,
				resolveExpressionAsBoolean(bindingExchange.durable()),
				resolveExpressionAsBoolean(bindingExchange.autoDelete()),
				resolveArguments(bindingExchange.arguments()));
	}

	private Map<String, Object> resolveArguments(Argument[] arguments) {
		Map<String, Object> map = new HashMap<>();
		for (Argument arg : arguments) {
			String key = resolveExpressionAsString(arg.name(), "@Argument.name");
			if (StringUtils.hasText(key)) {
				Object value = resolveExpression(arg.value());
				Object type = resolveExpression(arg.type());
				Class<?> typeClass;
				String typeName;
				if (type instanceof Class) {
					typeClass = (Class<?>) type;
					typeName = typeClass.getName();
				} else {
					Assert.isTrue(type instanceof String, "Type must resolve to a Class or String, but resolved to ["
							+ type.getClass().getName() + "]");
					typeName = (String) type;
					try {
						typeClass = ClassUtils.forName(typeName, this.beanClassLoader);
					} catch (Exception e) {
						throw new IllegalStateException("Could not load class", e);
					}
				}
				addToMap(map, key, value, typeClass, typeName);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("@Argument ignored because the name resolved to an empty String");
				}
			}
		}
		return map.size() < 1 ? null : map;
	}

	private void addToMap(Map<String, Object> map, String key, Object value, Class<?> typeClass, String typeName) {
		if (value.getClass().getName().equals(typeName)) {
			if (typeClass.equals(String.class) && !StringUtils.hasText((String) value)) {
				putEmpty(map, key);
			} else {
				map.put(key, value);
			}
		} else {
			if (value instanceof String && !StringUtils.hasText((String) value)) {
				putEmpty(map, key);
			} else {
				if (conversionService.canConvert(value.getClass(), typeClass)) {
					map.put(key, conversionService.convert(value, typeClass));
				} else {
					throw new IllegalStateException("Cannot convert from " + value.getClass().getName()
							+ " to " + typeName);
				}
			}
		}
	}

	private void putEmpty(Map<String, Object> map, String key) {
		if (this.emptyStringArguments.contains(key)) {
			map.put(key, "");
		} else {
			map.put(key, null);
		}
	}

	private boolean resolveExpressionAsBoolean(String value) {
		Object resolved = resolveExpression(value);
		if (resolved instanceof Boolean) {
			return (Boolean) resolved;
		} else if (resolved instanceof String) {
			return Boolean.valueOf((String) resolved);
		} else {
			return false;
		}
	}

	private String resolveExpressionAsString(String value, String attribute) {
		Object resolved = resolveExpression(value);
		if (resolved instanceof String) {
			return (String) resolved;
		} else {
			throw new IllegalStateException("The [" + attribute + "] must resolve to a String. "
					+ "Resolved to [" + resolved.getClass() + "] for [" + value + "]");
		}
	}

	private Object resolveExpression(String value) {
		String resolvedValue = resolve(value);
		if (!(resolvedValue.startsWith("#{") && value.endsWith("}"))) {
			return resolvedValue;
		}
		return this.resolver.evaluate(resolvedValue, this.expressionContext);
	}

	private String resolve(String value) {
		if (this.beanFactory != null) {
			return this.beanFactory.resolveEmbeddedValue(value);
		}
		return value;
	}

	private class AmqpHandlerMethodFactoryAdapter implements MessageHandlerMethodFactory {

		private MessageHandlerMethodFactory messageHandlerMethodFactory;

		public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory rabbitHandlerMethodFactory1) {
			this.messageHandlerMethodFactory = rabbitHandlerMethodFactory1;
		}

		@Override
		public InvocableHandlerMethod createInvocableHandlerMethod(Object bean, Method method) {
			return getMessageHandlerMethodFactory().createInvocableHandlerMethod(bean, method);
		}

		private MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
			if (this.messageHandlerMethodFactory == null) {
				this.messageHandlerMethodFactory = createDefaultMessageHandlerMethodFactory();
			}
			return this.messageHandlerMethodFactory;
		}

		private MessageHandlerMethodFactory createDefaultMessageHandlerMethodFactory() {
			DefaultMessageHandlerMethodFactory defaultFactory = new DefaultMessageHandlerMethodFactory();
			defaultFactory.setBeanFactory(FakeAmqpListenerAnnotationBeanPostProcessor.this.beanFactory);
			defaultFactory.afterPropertiesSet();
			return defaultFactory;
		}
	}

	private static class TypeMetadata {

		final ListenerMethod[] listenerMethods;

		final Method[] handlerMethods;

		final FakeAmqpListener[] classAnnotations;

		static final TypeMetadata EMPTY = new TypeMetadata();

		private TypeMetadata() {
			this.listenerMethods = new ListenerMethod[0];
			this.handlerMethods = new Method[0];
			this.classAnnotations = new FakeAmqpListener[0];
		}

		TypeMetadata(ListenerMethod[] methods, Method[] multiMethods, FakeAmqpListener[] classLevelListeners) {
			this.listenerMethods = methods;
			this.handlerMethods = multiMethods;
			this.classAnnotations = classLevelListeners;
		}
	}

	private static class ListenerMethod {

		final Method method;

		final FakeAmqpListener[] annotations;

		ListenerMethod(Method method, FakeAmqpListener[] annotations) {
			this.method = method;
			this.annotations = annotations;
		}
	}

	private Method checkMethodProxyAndReturn(Method method, Object bean) {
		if (AopUtils.isJdkDynamicProxy(bean)) {
			try {
				// Found a @RabbitListener method on the target class for this JDK proxy ->
				// is it also present on the proxy itself?
				method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
				Class<?>[] proxiedInterfaces = ((Advised) bean).getProxiedInterfaces();
				for (Class<?> proxiedInterface : proxiedInterfaces) {
					try {
						method = proxiedInterface.getMethod(method.getName(), method.getParameterTypes());
						break;
					} catch (NoSuchMethodException noMethod) {
						//ignore
					}
				}
			} catch (SecurityException ex) {
				ReflectionUtils.handleReflectionException(ex);
			} catch (NoSuchMethodException ex) {
				throw new IllegalStateException(String.format(
						"@FakeAmqpListener method '%s' found on bean target class '%s', " +
								"but not found in any interface(s) for bean JDK proxy. Either " +
								"pull the method up to an interface or switch to subclass (CGLIB) " +
								"proxies by setting proxy-target-class/proxyTargetClass " +
								"attribute to 'true'", method.getName(), method.getDeclaringClass().getSimpleName()));
			}
		}
		return method;
	}
}
