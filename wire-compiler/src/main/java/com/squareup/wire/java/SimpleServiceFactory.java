/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.wire.java;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.wire.internal.Util;
import com.squareup.wire.model.ProtoTypeName;
import com.squareup.wire.model.WireRpc;
import com.squareup.wire.model.WireService;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Modifier;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public class SimpleServiceFactory implements ServiceFactory {
  @Override public TypeSpec create(
      JavaGenerator javaGenerator, List<String> options, WireService service) {
    ClassName interfaceName = (ClassName) javaGenerator.typeName(service.protoTypeName());

    TypeSpec.Builder typeBuilder = TypeSpec.interfaceBuilder(interfaceName.simpleName());
    typeBuilder.addModifiers(Modifier.PUBLIC);

    if (!service.documentation().isEmpty()) {
      typeBuilder.addJavadoc("$L\n", Util.sanitizeJavadoc(service.documentation()));
    }

    for (WireRpc rpc : service.rpcs()) {
      ProtoTypeName requestType = rpc.requestType();
      TypeName requestJavaType = javaGenerator.typeName(requestType);
      ProtoTypeName responseType = rpc.responseType();
      TypeName responseJavaType = javaGenerator.typeName(responseType);

      MethodSpec.Builder rpcBuilder = MethodSpec.methodBuilder(upperToLowerCamel(rpc.name()));
      rpcBuilder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
      rpcBuilder.returns(responseJavaType);
      rpcBuilder.addException(IOException.class);

      rpcBuilder.addParameter(requestJavaType, upperToLowerCamel(requestType.simpleName()));

      if (!rpc.documentation().isEmpty()) {
        rpcBuilder.addJavadoc("$L\n", Util.sanitizeJavadoc(rpc.documentation()));
      }

      typeBuilder.addMethod(rpcBuilder.build());
    }

    return typeBuilder.build();
  }

  private String upperToLowerCamel(String string) {
    return UPPER_CAMEL.to(LOWER_CAMEL, string);
  }
}
