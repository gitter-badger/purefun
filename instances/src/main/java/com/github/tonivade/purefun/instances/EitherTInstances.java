/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.instances;

import static com.github.tonivade.purefun.Function1.cons;
import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purefun.Unit.unit;
import java.time.Duration;
import com.github.tonivade.purefun.Consumer1;
import com.github.tonivade.purefun.Eq;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.transformer.EitherT;
import com.github.tonivade.purefun.transformer.EitherTOf;
import com.github.tonivade.purefun.transformer.EitherT_;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.typeclasses.Bracket;
import com.github.tonivade.purefun.typeclasses.Defer;
import com.github.tonivade.purefun.typeclasses.Monad;
import com.github.tonivade.purefun.typeclasses.MonadDefer;
import com.github.tonivade.purefun.typeclasses.MonadError;
import com.github.tonivade.purefun.typeclasses.MonadThrow;
import com.github.tonivade.purefun.typeclasses.Reference;

public interface EitherTInstances {

  static <F extends Witness, L, R> Eq<Kind<Kind<Kind<EitherT_, F>, L>, R>> eq(Eq<Kind<F, Either<L, R>>> eq) {
    return (a, b) -> eq.eqv(EitherTOf.narrowK(a).value(), EitherTOf.narrowK(b).value());
  }

  static <F extends Witness, L> Monad<Kind<Kind<EitherT_, F>, L>> monad(Monad<F> monadF) {
    return EitherTMonad.instance(checkNonNull(monadF));
  }

  static <F extends Witness, L> MonadError<Kind<Kind<EitherT_, F>, L>, L> monadError(Monad<F> monadF) {
    return EitherTMonadErrorFromMonad.instance(checkNonNull(monadF));
  }

  static <F extends Witness, L> MonadError<Kind<Kind<EitherT_, F>, L>, L> monadError(MonadError<F, L> monadErrorF) {
    return EitherTMonadErrorFromMonadError.instance(checkNonNull(monadErrorF));
  }

  static <F extends Witness> MonadThrow<Kind<Kind<EitherT_, F>, Throwable>> monadThrow(Monad<F> monadF) {
    return EitherTMonadThrowFromMonad.instance(checkNonNull(monadF));
  }

  static <F extends Witness> MonadThrow<Kind<Kind<EitherT_, F>, Throwable>> monadThrow(MonadThrow<F> monadF) {
    return EitherTMonadThrowFromMonadThrow.instance(checkNonNull(monadF));
  }

  static <F extends Witness, L> Defer<Kind<Kind<EitherT_, F>, L>> defer(MonadDefer<F> monadDeferF) {
    return EitherTDefer.instance(checkNonNull(monadDeferF));
  }

  static <F extends Witness> MonadDefer<Kind<Kind<EitherT_, F>, Throwable>> monadDeferFromMonad(MonadDefer<F> monadDeferF) {
    return EitherTMonadDeferFromMonad.instance(checkNonNull(monadDeferF));
  }

  static <F extends Witness> MonadDefer<Kind<Kind<EitherT_, F>, Throwable>> monadDeferFromMonadThrow(MonadDefer<F> monadDeferF) {
    return EitherTMonadDeferFromMonadThrow.instance(checkNonNull(monadDeferF));
  }

  static <F extends Witness, A>
         Reference<Kind<Kind<EitherT_, F>, Throwable>, A>
         refFromMonad(MonadDefer<F> monadDeferF, A value) {
    return Reference.of(monadDeferFromMonad(monadDeferF), value);
  }

  static <F extends Witness, A>
         Reference<Kind<Kind<EitherT_, F>, Throwable>, A>
         refFromMonadThrow(MonadDefer<F> monadDeferF, A value) {
    return Reference.of(monadDeferFromMonadThrow(monadDeferF), value);
  }
}

interface EitherTMonad<F extends Witness, L> extends Monad<Kind<Kind<EitherT_, F>, L>> {

  static <F extends Witness, L> EitherTMonad<F, L> instance(Monad<F> monadF) {
    return () -> monadF;
  }

  Monad<F> monadF();

  @Override
  default <T> EitherT<F, L, T> pure(T value) {
    return EitherT.<F, L, T>right(monadF(), value);
  }

  @Override
  default <T, R> EitherT<F, L, R> flatMap(Kind<Kind<Kind<EitherT_, F>, L>, T> value,
      Function1<T, ? extends Kind<Kind<Kind<EitherT_, F>, L>, R>> map) {
    return EitherTOf.narrowK(value).flatMap(map.andThen(EitherTOf::narrowK));
  }
}

interface EitherTMonadErrorFromMonad<F extends Witness, E>
    extends MonadError<Kind<Kind<EitherT_, F>, E>, E>, EitherTMonad<F, E> {

  static <F extends Witness, L> EitherTMonadErrorFromMonad<F, L> instance(Monad<F> monadF) {
    return () -> monadF;
  }

  @Override
  default <A> EitherT<F, E, A> raiseError(E error) {
    return EitherT.<F, E, A>left(monadF(), error);
  }

  @Override
  default <A> EitherT<F, E, A> handleErrorWith(Kind<Kind<Kind<EitherT_, F>, E>, A> value,
      Function1<E, ? extends Kind<Kind<Kind<EitherT_, F>, E>, A>> handler) {
    return EitherT.of(monadF(),
        monadF().flatMap(EitherTOf.narrowK(value).value(),
            either -> either.fold(
                e -> handler.andThen(EitherTOf::narrowK).apply(e).value(),
                a -> monadF().pure(Either.<E, A>right(a)))));
  }
}

interface EitherTMonadErrorFromMonadError<F extends Witness, E>
    extends MonadError<Kind<Kind<EitherT_, F>, E>, E>,
            EitherTMonad<F, E> {

  static <F extends Witness, E> EitherTMonadErrorFromMonadError<F, E> instance(MonadError<F, E> monadErrorF) {
    return () -> monadErrorF;
  }

  @Override
  MonadError<F, E> monadF();

  @Override
  default <A> EitherT<F, E, A> raiseError(E error) {
    return EitherT.<F, E, A>of(monadF(), monadF().raiseError(error));
  }

  @Override
  default <A> EitherT<F, E, A> handleErrorWith(Kind<Kind<Kind<EitherT_, F>, E>, A> value,
      Function1<E, ? extends Kind<Kind<Kind<EitherT_, F>, E>, A>> handler) {
    return EitherT.of(monadF(),
                      monadF().handleErrorWith(EitherTOf.narrowK(value).value(),
                                               error -> handler.andThen(EitherTOf::narrowK).apply(error).value()));
  }
}

interface EitherTMonadThrowFromMonad<F extends Witness>
    extends EitherTMonadErrorFromMonad<F, Throwable>,
            MonadThrow<Kind<Kind<EitherT_, F>, Throwable>> {

  static <F extends Witness> EitherTMonadThrowFromMonad<F> instance(Monad<F> monadF) {
    return () -> monadF;
  }
}

interface EitherTMonadThrowFromMonadThrow<F extends Witness>
    extends EitherTMonadErrorFromMonadError<F, Throwable>,
            MonadThrow<Kind<Kind<EitherT_, F>, Throwable>> {

  static <F extends Witness> EitherTMonadThrowFromMonadThrow<F> instance(MonadThrow<F> monadThrowF) {
    return () -> monadThrowF;
  }
}

interface EitherTDefer<F extends Witness, E> extends Defer<Kind<Kind<EitherT_, F>, E>> {

  static <F extends Witness, E> EitherTDefer<F, E> instance(MonadDefer<F> monadDeferF) {
    return () -> monadDeferF;
  }

  MonadDefer<F> monadF();

  @Override
  default <A> EitherT<F, E, A> defer(Producer<Kind<Kind<Kind<EitherT_, F>, E>, A>> defer) {
    return EitherT.of(monadF(), monadF().defer(() -> defer.map(EitherTOf::narrowK).get().value()));
  }
}

interface EitherTBracket<F extends Witness> extends Bracket<Kind<Kind<EitherT_, F>, Throwable>> {

  MonadDefer<F> monadF();

  <A> Kind<F, Either<Throwable, A>> acquireRecover(Throwable error);

  @Override
  default <A, B> EitherT<F, Throwable, B>
          bracket(Kind<Kind<Kind<EitherT_, F>, Throwable>, A> acquire,
                  Function1<A, ? extends Kind<Kind<Kind<EitherT_, F>, Throwable>, B>> use,
                  Consumer1<A> release) {
    Kind<F, Either<Throwable, B>> bracket =
        monadF().bracket(
            acquire.fix(EitherTOf::narrowK).value(),
            either -> either.fold(
                this::acquireRecover,
                value -> use.andThen(EitherTOf::narrowK).apply(value).value()),
            either -> either.fold(cons(unit()), release.asFunction()));
    return EitherT.of(monadF(), bracket);
  }
}

interface EitherTMonadDeferFromMonad<F extends Witness>
    extends EitherTMonadThrowFromMonad<F>,
            EitherTDefer<F, Throwable>,
            EitherTBracket<F>,
            MonadDefer<Kind<Kind<EitherT_, F>, Throwable>> {

  static <F extends Witness> EitherTMonadDeferFromMonad<F> instance(MonadDefer<F> monadDeferF) {
    return () -> monadDeferF;
  }

  @Override
  default <A> Kind<F, Either<Throwable, A>> acquireRecover(Throwable error) {
    return monadF().pure(Either.left(error));
  }

  @Override
  default EitherT<F, Throwable, Unit> sleep(Duration duration) {
    return EitherT.<F, Throwable, Unit>of(monadF(), monadF().map(monadF().sleep(duration), Either::right));
  }
}

interface EitherTMonadDeferFromMonadThrow<F extends Witness>
    extends EitherTMonadThrowFromMonadThrow<F>,
            EitherTDefer<F, Throwable>,
            EitherTBracket<F>,
            MonadDefer<Kind<Kind<EitherT_, F>, Throwable>> {

  static <F extends Witness> EitherTMonadDeferFromMonadThrow<F> instance(MonadDefer<F> monadDeferF) {
    return () -> monadDeferF;
  }

  @Override
  default <A> Kind<F, Either<Throwable, A>> acquireRecover(Throwable error) {
    return monadF().raiseError(error);
  }

  @Override
  default EitherT<F, Throwable, Unit> sleep(Duration duration) {
    return EitherT.<F, Throwable, Unit>of(monadF(), monadF().map(monadF().sleep(duration), Either::right));
  }
}
