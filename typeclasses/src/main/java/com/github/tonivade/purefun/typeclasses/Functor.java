/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.typeclasses;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;

public interface Functor<F extends Witness> extends Invariant<F> {

  <T, R> Kind<F, R> map(Kind<F, T> value, Function1<T, R> map);

  default <A, B> Function1<Kind<F, A>, Kind<F, B>> lift(Function1<A, B> function) {
    return fa -> map(fa, function);
  }

  @Override
  default <A, B> Kind<F, B> imap(Kind<F, A> value, Function1<A, B> map, Function1<B, A> comap) {
    return map(value, map);
  }

  static <F extends Witness, G extends Witness> Functor<Nested<F, G>> compose(Functor<F> f, Functor<G> g) {
    return new ComposedFunctor<F, G>() {

      @Override
      public Functor<F> f() { return f; }

      @Override
      public Functor<G> g() { return g; }
    };
  }

  static <F extends Witness, G extends Witness> Functor<Nested<F, G>> compose(Contravariant<F> f, Contravariant<G> g) {
    return new ComposedContravariant<F, G>() {

      @Override
      public Contravariant<F> f() { return f; }

      @Override
      public Contravariant<G> g() { return g; }
    };
  }
}