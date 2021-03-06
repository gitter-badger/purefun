/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.transformer;

import static com.github.tonivade.purefun.Function1.identity;
import static com.github.tonivade.purefun.Precondition.checkNonNull;
import static com.github.tonivade.purefun.Producer.cons;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.HigherKind;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.github.tonivade.purefun.typeclasses.FunctionK;
import com.github.tonivade.purefun.typeclasses.Monad;

@HigherKind(sealed = true)
public interface EitherT<F extends Witness, L, R> extends EitherTOf<F, L, R> {

  Monad<F> monad();
  Kind<F, Either<L, R>> value();

  default <V> EitherT<F, L, V> map(Function1<R, V> map) {
    return EitherT.of(monad(), monad().map(value(), v -> v.map(map)));
  }

  default <V> EitherT<F, L, V> flatMap(Function1<R, EitherT<F, L, V>> map) {
    return EitherT.of(monad(), flatMapF(v -> map.apply(v).value()));
  }

  default <T, V> EitherT<F, T, V> bimap(Function1<L, T> leftMapper, Function1<R, V> rightMapper) {
    return EitherT.of(monad(), monad().map(value(), v -> v.bimap(leftMapper, rightMapper)));
  }

  default <T> EitherT<F, T, R> mapLeft(Function1<L, T> leftMapper) {
    return EitherT.of(monad(), monad().map(value(), v -> v.mapLeft(leftMapper)));
  }

  default <V> Kind<F, V> fold(Function1<L, V> leftMapper, Function1<R, V> rightMapper) {
    return monad().map(value(), v -> v.fold(leftMapper, rightMapper));
  }

  default <G extends Witness> EitherT<G, L, R> mapK(Monad<G> other, FunctionK<F, G> functionK) {
    return EitherT.of(other, functionK.apply(value()));
  }

  default EitherT<F, L, R> filterOrElse(Matcher1<R> filter, Producer<Either<L, R>> orElse) {
    return EitherT.of(monad(), monad().map(value(), v -> v.filterOrElse(filter, orElse)));
  }

  default EitherT<F, R, L> swap() {
    return EitherT.of(monad(), monad().map(value(), Either::swap));
  }

  default Kind<F, Boolean> isRight() {
    return monad().map(value(), Either::isRight);
  }

  default Kind<F, Boolean> isLeft() {
    return monad().map(value(), Either::isLeft);
  }

  default Kind<F, L> getLeft() {
    return monad().map(value(), Either::getLeft);
  }

  default Kind<F, R> getRight() {
    return monad().map(value(), Either::getRight);
  }

  default Kind<F, R> get() {
    return getRight();
  }

  default Kind<F, R> getOrElse(R orElse) {
    return getOrElse(cons(orElse));
  }

  default Kind<F, R> getOrElse(Producer<R> orElse) {
    return fold(left -> orElse.get(), identity());
  }

  default OptionT<F, R> toOption() {
    return OptionT.of(monad(), monad().map(value(), Either::toOption));
  }

  static <F extends Witness, L, R> EitherT<F, L, R> lift(Monad<F> monad, Either<L, R> either) {
    return of(monad, monad.pure(either));
  }

  static <F extends Witness, L, R> EitherT<F, L, R> of(Monad<F> monad, Kind<F, Either<L, R>> value) {
    checkNonNull(monad);
    checkNonNull(value);
    return new SealedEitherT<F, L, R>() {

      @Override
      public Monad<F> monad() { return monad; }

      @Override
      public Kind<F, Either<L, R>> value() { return value; }
    };
  }

  static <F extends Witness, L, R> EitherT<F, L, R> right(Monad<F> monad, R right) {
    return lift(monad, Either.right(right));
  }

  static <F extends Witness, L, R> EitherT<F, L, R> left(Monad<F> monad, L left) {
    return lift(monad, Either.left(left));
  }

  static <F extends Witness, R> EitherT<F, Throwable, R> fromOption(Monad<F> monad, Option<R> value) {
    return lift(monad, value.toEither());
  }

  static <F extends Witness, R> EitherT<F, Throwable, R> fromTry(Monad<F> monad, Try<R> value) {
    return lift(monad, value.toEither());
  }

  default <V> Kind<F, Either<L, V>> flatMapF(Function1<R, Kind<F, Either<L, V>>> map) {
   return monad().flatMap(value(), v -> v.fold(left -> monad().pure(Either.left(left)), map));
  }
}