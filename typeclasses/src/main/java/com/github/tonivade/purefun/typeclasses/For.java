/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.typeclasses;

import static com.github.tonivade.purefun.Producer.cons;
import static com.github.tonivade.purefun.Unit.unit;
import static com.github.tonivade.purefun.Precondition.checkNonNull;

import com.github.tonivade.purefun.Consumer1;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.Producer;

public final class For<F extends Witness> {

  private final Monad<F> monad;

  private For(Monad<F> monad) {
    this.monad = checkNonNull(monad);
  }

  public <T> For1<F, T> and(T next) {
    return For.with(monad, monad.pure(next));
  }

  public <T> For1<F, T> then(Kind<F, T> next) {
    return For.with(monad, next);
  }

  public <T> For1<F, T> andThen(Producer<? extends Kind<F, T>> next) {
    return For.with(monad, monad.andThen(monad.pure(unit()), next));
  }

  public static <F extends Witness> For<F> with(Monad<F> monad) {
    return new For<>(monad);
  }

  public static <F extends Witness, T> For1<F, T> with(Monad<F> monad, Kind<F, T> value1) {
    return new For1<>(monad, cons(value1));
  }
}

abstract class AbstractFor<F extends Witness, A, B> {

  protected final Monad<F> monad;
  protected final Function1<A, ? extends Kind<F, B>> value;

  protected AbstractFor(Monad<F> monad, Function1<A, ? extends Kind<F, B>> value) {
    this.monad = checkNonNull(monad);
    this.value = checkNonNull(value);
  }

  public abstract Kind<F, B> run();

  public <R> R fix(Function1<Kind<F, B>, R> fix) {
    return fix.apply(run());
  }

  public void end(Consumer1<Kind<F, B>> consumer) {
    consumer.accept(run());
  }

  public <R> Kind<F, R> returns(R value) {
    return monad.map(run(), ignore -> value);
  }
}
