/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.instances;

import static com.github.tonivade.purefun.Unit.unit;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Tuple;
import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.monad.State;
import com.github.tonivade.purefun.monad.StateOf;
import com.github.tonivade.purefun.monad.State_;
import com.github.tonivade.purefun.typeclasses.Console;
import com.github.tonivade.purefun.typeclasses.Monad;
import com.github.tonivade.purefun.typeclasses.MonadState;

@SuppressWarnings("unchecked")
public interface StateInstances {

  static <S> Monad<Kind<State_, S>> monad() {
    return StateMonad.INSTANCE;
  }

  static <S> MonadState<Kind<State_, S>, S> monadState() {
    return StateMonadState.INSTANCE;
  }

  static Console<Kind<State_, ImmutableList<String>>> console() {
    return ConsoleState.INSTANCE;
  }
}

interface StateMonad<S> extends Monad<Kind<State_, S>> {

  @SuppressWarnings("rawtypes")
  StateMonad INSTANCE = new StateMonad() {};

  @Override
  default <T> State<S, T> pure(T value) {
    return State.<S, T>pure(value);
  }

  @Override
  default <T, R> State<S, R> flatMap(Kind<Kind<State_, S>, T> value,
      Function1<T, ? extends Kind<Kind<State_, S>, R>> map) {
    return StateOf.narrowK(value).flatMap(map.andThen(StateOf::narrowK));
  }
}

interface StateMonadState<S> extends MonadState<Kind<State_, S>, S>, StateMonad<S> {

  @SuppressWarnings("rawtypes")
  StateMonadState INSTANCE = new StateMonadState() {};

  @Override
  default State<S, S> get() {
    return State.<S>get();
  }

  @Override
  default State<S, Unit> set(S state) {
    return State.set(state);
  }
}

final class ConsoleState implements Console<Kind<State_, ImmutableList<String>>> {

  protected static final ConsoleState INSTANCE = new ConsoleState();

  @Override
  public State<ImmutableList<String>, String> readln() {
    return State.<ImmutableList<String>, String>state(list -> Tuple.of(list.tail(), list.head().get()));
  }

  @Override
  public State<ImmutableList<String>, Unit> println(String text) {
    return State.<ImmutableList<String>, Unit>state(list -> Tuple.of(list.append(text), unit()));
  }
}