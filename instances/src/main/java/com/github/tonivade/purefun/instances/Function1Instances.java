/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.instances;

import static com.github.tonivade.purefun.typeclasses.Conested.conest;
import static com.github.tonivade.purefun.typeclasses.Conested.counnest;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Function1Of;
import com.github.tonivade.purefun.Function1_;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.typeclasses.Applicative;
import com.github.tonivade.purefun.typeclasses.Conested;
import com.github.tonivade.purefun.typeclasses.Contravariant;
import com.github.tonivade.purefun.typeclasses.Functor;
import com.github.tonivade.purefun.typeclasses.Monad;
import com.github.tonivade.purefun.typeclasses.Profunctor;

@SuppressWarnings("unchecked")
public interface Function1Instances {

  static <T> Functor<Kind<Function1_, T>> functor() {
    return Function1Functor.INSTANCE;
  }

  static <T> Applicative<Kind<Function1_, T>> applicative() {
    return Function1Applicative.INSTANCE;
  }

  static <T> Monad<Kind<Function1_, T>> monad() {
    return Function1Monad.INSTANCE;
  }

  static <T> Contravariant<Conested<Function1_, T>> contravariant() {
    return Function1Contravariant.INSTANCE;
  }

  static Profunctor<Function1_> profunctor() {
    return Function1Profunctor.INSTANCE;
  }
}

interface Function1Functor<T> extends Functor<Kind<Function1_, T>> {

  @SuppressWarnings("rawtypes")
  Function1Functor INSTANCE = new Function1Functor() {};

  @Override
  default <A, R> Function1<T, R> map(Kind<Kind<Function1_, T>, A> value, Function1<A, R> map) {
    Function1<T, A> function = value.fix(Function1Of::narrowK);
    return function.andThen(map);
  }
}

interface Function1Pure<T> extends Applicative<Kind<Function1_, T>> {

  @Override
  default <A> Function1<T, A> pure(A value) {
    return Function1.<T, A>cons(value);
  }
}

interface Function1Applicative<T> extends Function1Pure<T> {

  @SuppressWarnings("rawtypes")
  Function1Applicative INSTANCE = new Function1Applicative() {};

  @Override
  default <A, R> Function1<T, R> ap(Kind<Kind<Function1_, T>, A> value, Kind<Kind<Function1_, T>, Function1<A, R>> apply) {
    Function1<T, A> function = value.fix(Function1Of::narrowK);
    Function1<T, Function1<A, R>> map = apply.fix(Function1Of::narrowK);
    return function.flatMap(a -> map.andThen(f -> f.apply(a)));
  }
}

interface Function1Monad<T> extends Function1Pure<T>, Monad<Kind<Function1_, T>> {

  @SuppressWarnings("rawtypes")
  Function1Monad INSTANCE = new Function1Monad() {};

  @Override
  default <A, R> Function1<T, R> flatMap(Kind<Kind<Function1_, T>, A> value, Function1<A, ? extends Kind<Kind<Function1_, T>, R>> map) {
    Function1<T, A> function = value.fix(Function1Of::narrowK);
    return function.flatMap(map.andThen(Function1Of::narrowK));
  }
}

interface Function1Contravariant<R> extends Contravariant<Conested<Function1_, R>> {

  @SuppressWarnings("rawtypes")
  Function1Contravariant INSTANCE = new Function1Contravariant() {};

  @Override
  default <A, B> Kind<Conested<Function1_, R>, B> contramap(Kind<Conested<Function1_, R>, A> value, Function1<B, A> map) {
    Function1<A, R> function = counnest(value).fix(Function1Of::narrowK);
    return conest(function.compose(map));
  }
}

interface Function1Profunctor extends Profunctor<Function1_> {

  Function1Profunctor INSTANCE = new Function1Profunctor() {};

  @Override
  default <A, B, C, D> Function1<C, D> dimap(Kind<Kind<Function1_, A>, B> value, Function1<C, A> contramap, Function1<B, D> map) {
    Function1<A, B> function = value.fix(Function1Of::narrowK);
    return function.compose(contramap).andThen(map);
  }
}
