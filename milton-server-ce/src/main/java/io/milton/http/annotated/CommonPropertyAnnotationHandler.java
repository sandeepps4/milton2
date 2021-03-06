/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.http.annotated;

/**
 *
 * @author brad
 */
public class CommonPropertyAnnotationHandler<T> extends AbstractAnnotationHandler {

	private T defaultValue;
	protected final String[] propertyNames;

	public CommonPropertyAnnotationHandler(Class annoClass, final AnnotationResourceFactory outer) {
		super(outer, annoClass);
		propertyNames = new String[0];
	}

	public CommonPropertyAnnotationHandler(Class annoClass, final AnnotationResourceFactory outer, String... propNames) {
		super(outer, annoClass);
		propertyNames = propNames;
	}

	public T get(AnnoResource res) {
		Object source = res.getSource();
		try {
			ControllerMethod cm = getBestMethod(source.getClass(), null, null, Object.class);
			if (cm != null) {
				T val = (T) invoke(cm, res);
				return val;
			} else {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = annoResourceFactory.findMethodForAnno(source.getClass(), annoClass);
				if (m != null && m.getParameterTypes().length ==0 ) {
					T val = (T) m.invoke(source);
					return val;
				}
				for (String propName : propertyNames) {
					Object s = attemptToReadProperty(source, propName);
					if (s != null) {
						return (T) s;
					}
				}
				return deriveDefaultValue(source);
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + annoClass + " - " + source.getClass(), e);
		}
	}

	public void set(AnnoResource res, T newValue) {
		Object source = res.getSource();
		try {
			ControllerMethod cm = getBestMethod(source.getClass(), null, null, Void.TYPE);
			if (cm == null) {
				// look for an annotation on the source itself
				java.lang.reflect.Method m = annoResourceFactory.findMethodForAnno(source.getClass(), annoClass);
				if (m != null) {
					m.invoke(source, (Object) null);
					return;
				}
				// look for a bean property
				for (String propName : propertyNames) {
					if (attemptToSetProperty(source, propName)) {
						return;
					}
				}
			} else {
				invoke(cm, res, newValue);
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception executing " + annoClass + " - " + source.getClass(), e);
		}

	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	protected T deriveDefaultValue(Object source) {
		return getDefaultValue();
	}
}
