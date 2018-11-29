/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun;

@FunctionalInterface
public interface Matcher2<A, B> {

  boolean apply(A a, B b);
  
  default Matcher1<Tuple2<A, B>> tupled() {
    return tuple -> apply(tuple.get1(), tuple.get2());
  }

  static <A, B> Matcher2<A, B> otherwise() {
    return (a, b) -> true;
  }
}