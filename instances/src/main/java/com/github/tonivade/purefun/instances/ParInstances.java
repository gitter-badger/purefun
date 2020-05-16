/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.instances;

import com.github.tonivade.purefun.Consumer1;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Instance;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.concurrent.Par;
import com.github.tonivade.purefun.concurrent.Par_;
import com.github.tonivade.purefun.typeclasses.Applicative;
import com.github.tonivade.purefun.typeclasses.Bracket;
import com.github.tonivade.purefun.typeclasses.Defer;
import com.github.tonivade.purefun.typeclasses.Functor;
import com.github.tonivade.purefun.typeclasses.Monad;
import com.github.tonivade.purefun.typeclasses.MonadDefer;
import com.github.tonivade.purefun.typeclasses.MonadThrow;

import java.time.Duration;

import static com.github.tonivade.purefun.Function1.identity;

public interface ParInstances {

  static Functor<Par_> functor() {
    return ParFunctor.instance();
  }

  static Applicative<Par_> applicative() {
    return PureApplicative.instance();
  }

  static Monad<Par_> monad() {
    return ParMonad.instance();
  }

  static MonadDefer<Par_> monadDefer() {
    return ParMonadDefer.instance();
  }
}

@Instance
interface ParFunctor extends Functor<Par_> {
  @Override
  default <T, R> Higher1<Par_, R> map(Higher1<Par_, T> value, Function1<T, R> mapper) {
    return value.fix1(Par_::narrowK).map(mapper);
  }
}

interface ParPure extends Applicative<Par_> {
  @Override
  default <T> Higher1<Par_, T> pure(T value) {
    return Par.success(value);
  }
}

@Instance
interface PureApplicative extends ParPure {
  @Override
  default <T, R> Higher1<Par_, R> ap(Higher1<Par_, T> value, Higher1<Par_, Function1<T, R>> apply) {
    return value.fix1(Par_::narrowK).ap(apply.fix1(Par_::narrowK));
  }
}

@Instance
interface ParMonad extends ParPure, Monad<Par_> {
  @Override
  default <T, R> Higher1<Par_, R> flatMap(Higher1<Par_, T> value, Function1<T, ? extends Higher1<Par_, R>> map) {
    return value.fix1(Par_::narrowK).flatMap(x -> map.apply(x).fix1(Par_::narrowK));
  }
}

interface ParMonadThrow extends ParMonad, MonadThrow<Par_> {

  @Override
  default <A> Higher1<Par_, A> raiseError(Throwable error) {
    return Par.<A>failure(error);
  }

  @Override
  default <A> Higher1<Par_, A> handleErrorWith(Higher1<Par_, A> value,
                                                Function1<Throwable, ? extends Higher1<Par_, A>> handler) {
    return Par_.narrowK(value).fold(handler.andThen(Par_::narrowK), Par::success).flatMap(identity());
  }
}

interface ParDefer extends Defer<Par_> {

  @Override
  default <A> Higher1<Par_, A> defer(Producer<Higher1<Par_, A>> defer) {
    return Par.defer(defer.map(Par_::narrowK)::get);
  }
}

interface ParBracket extends Bracket<Par_> {

  @Override
  default <A, B> Higher1<Par_, B> bracket(
      Higher1<Par_, A> acquire, Function1<A, ? extends Higher1<Par_, B>> use, Consumer1<A> release) {
    return Par.bracket(Par_.narrowK(acquire), use.andThen(Par_::narrowK), release);
  }
}

@Instance
interface ParMonadDefer extends ParMonadThrow, ParDefer, ParBracket, MonadDefer<Par_> {

  @Override
  default Higher1<Par_, Unit> sleep(Duration duration) {
    return Par.sleep(duration);
  }
}
