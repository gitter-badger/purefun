/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun;

import com.github.tonivade.purefun.type.Option;

public interface PartialFunction3<A, B, C, R> {

  R apply(A a, B b, C c);

  boolean isDefinedAt(A a, B b, C c);

  default Function3<A, B, C, Option<R>> lift() {
    return (a, b, c) -> isDefinedAt(a, b, c) ? Option.some(apply(a, b, c)) : Option.none();
  }

  static <A, B, C, R> PartialFunction3<A, B, C, R> of(Matcher3<A, B, C> matcher, PartialFunction3<A, B, C, R> apply) {
    return new PartialFunction3<A, B, C, R>() {

      @Override
      public boolean isDefinedAt(A a, B b, C c) {
        return matcher.match(a, b, c);
      }

      @Override
      public R apply(A a, B b, C c) {
        return apply.apply(a, b, c);
      }
    };
  }
}
