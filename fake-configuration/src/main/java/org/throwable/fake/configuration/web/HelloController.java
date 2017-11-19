package org.throwable.fake.configuration.web;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 11:56
 */
@RestController
public class HelloController {

	@ApiOperation("问好")
	@GetMapping(value = "/hello")
	public String hello(@RequestParam(name = "name")String name){
		return String.format("%s say hello!", name);
	}

}
