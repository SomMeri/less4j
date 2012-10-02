package com.github.sommeri.less4j.core;

import com.github.sommeri.less4j.ILessCompiler;

public class DummyLessCompiler implements ILessCompiler {

	public String compile(String cssContent) {
		return cssContent;
	}

}
