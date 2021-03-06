/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.typeclasses;

import static com.github.tonivade.purefun.Unit.unit;
import static com.github.tonivade.purefun.data.Sequence.listOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.Unit;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.instances.SequenceInstances;
import com.github.tonivade.purefun.instances.ValidationInstances;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IOOf;
import com.github.tonivade.purefun.monad.IO_;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Eval;
import com.github.tonivade.purefun.type.Validation;
import com.github.tonivade.purefun.type.ValidationOf;
import com.github.tonivade.purefun.type.Validation_;

@ExtendWith(MockitoExtension.class)
public class SelectiveTest {

  private final Selective<IO_> monad = IOInstances.monad();

  @Test
  public void apply() {
    Selective<Kind<Validation_, Sequence<String>>> selective =
        ValidationInstances.selective(SequenceInstances.semigroup());

    Kind<Kind<Validation_, Sequence<String>>, Integer> validValue =
        Validation.<Sequence<String>, Integer>valid(1);
    Kind<Kind<Validation_, Sequence<String>>, Integer> invalidValue =
        Validation.<Sequence<String>, Integer>invalid(listOf("error 1"));
    Kind<Kind<Validation_, Sequence<String>>, Function1<Integer, String>> apply =
        Validation.<Sequence<String>, Function1<Integer, String>>valid(Function1.of(String::valueOf));
    Kind<Kind<Validation_, Sequence<String>>, Function1<Integer, String>> invalidApply =
        Validation.<Sequence<String>, Function1<Integer, String>>invalid(listOf("error 2"));

    assertEquals(Validation.valid("1"), selective.ap(validValue, apply).fix(ValidationOf::narrowK));
    assertEquals(Validation.invalid(listOf("error 1", "error 2")), selective.ap(invalidValue, invalidApply).fix(ValidationOf::narrowK));
    assertEquals(Validation.invalid(listOf("error 1")), selective.ap(invalidValue, apply).fix(ValidationOf::narrowK));
    assertEquals(Validation.invalid(listOf("error 2")), selective.ap(validValue, invalidApply).fix(ValidationOf::narrowK));
  }

  @Test
  public void select() {
    Function1<String, Integer> parseInt = Integer::parseInt;

    Kind<IO_, Integer> left = monad.select(monad.pure(Either.left("1")), monad.pure(parseInt));
    Kind<IO_, Integer> right = monad.select(monad.pure(Either.right(-1)), monad.pure(parseInt));

    assertAll(
        () -> assertEquals(1, left.fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals(-1, right.fix(IOOf::narrowK).unsafeRunSync())
    );
  }

  @Test
  public void branch() {
    Function1<String, Integer> parseInt = Integer::parseInt;
    Function1<String, Integer> countLetters = String::length;

    Kind<IO_, Integer> left = monad.branch(monad.pure(Either.left("1")), monad.pure(parseInt), monad.pure(countLetters));
    Kind<IO_, Integer> right = monad.branch(monad.pure(Either.right("asdfg")), monad.pure(parseInt), monad.pure(countLetters));

    assertAll(
        () -> assertEquals(1, left.fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals(5, right.fix(IOOf::narrowK).unsafeRunSync())
    );
  }

  @Test
  public void whenS(@Mock Producer<Unit> left, @Mock Producer<Unit> right) {
    when(left.get()).thenReturn(unit());

    IO<Unit> io1 = IO.task(left);
    IO<Unit> io2 = IO.task(right);

    monad.whenS(monad.pure(true), io1).fix(IOOf::narrowK).unsafeRunSync();
    monad.whenS(monad.pure(false), io2).fix(IOOf::narrowK).unsafeRunSync();

    verify(left).get();
    verify(right, never()).get();
  }

  @Test
  public void ifS() {
    Kind<IO_, String> left = monad.ifS(monad.pure(true), monad.pure("left"), monad.pure("right"));
    Kind<IO_, String> right = monad.ifS(monad.pure(false), monad.pure("left"), monad.pure("right"));

    assertAll(
        () -> assertEquals("left", left.fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals("right", right.fix(IOOf::narrowK).unsafeRunSync())
    );
  }

  @Test
  public void andS() {
    Kind<IO_, Boolean> and00 = monad.andS(monad.pure(false), monad.pure(false));
    Kind<IO_, Boolean> and01 = monad.andS(monad.pure(false), monad.pure(true));
    Kind<IO_, Boolean> and10 = monad.andS(monad.pure(true), monad.pure(false));
    Kind<IO_, Boolean> and11 = monad.andS(monad.pure(true), monad.pure(true));

    assertAll(
        () -> assertEquals(false, and00.fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals(false, and01.fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals(false, and10.fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals(true, and11.fix(IOOf::narrowK).unsafeRunSync())
    );
  }

  @Test
  public void orS() {
    Kind<IO_, Boolean> or00 = monad.orS(monad.pure(false), monad.pure(false));
    Kind<IO_, Boolean> or01 = monad.orS(monad.pure(false), monad.pure(true));
    Kind<IO_, Boolean> or10 = monad.orS(monad.pure(true), monad.pure(false));
    Kind<IO_, Boolean> or11 = monad.orS(monad.pure(true), monad.pure(true));

    assertAll(
        () -> assertEquals(false, or00.fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals(true, or01.fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals(true, or10.fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals(true, or11.fix(IOOf::narrowK).unsafeRunSync())
    );
  }

  @Test
  public void allS() {
    Eval<Kind<IO_, Boolean>> match =
        monad.allS(SequenceInstances.foldable(),
            listOf("a", "b", "c"), a -> monad.pure(a.length() == 1));
    Eval<Kind<IO_, Boolean>> notMatch =
        monad.allS(SequenceInstances.foldable(),
            listOf("a", "b", "cd"), a -> monad.pure(a.length() == 1));

    assertAll(
        () -> assertEquals(true, match.value().fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals(false, notMatch.value().fix(IOOf::narrowK).unsafeRunSync())
    );
  }

  @Test
  public void anyS() {
    Eval<Kind<IO_, Boolean>> match =
        monad.anyS(SequenceInstances.foldable(),
            listOf("a", "b", "cd"), a -> monad.pure(a.length() > 1));
    Eval<Kind<IO_, Boolean>> notMatch =
        monad.anyS(SequenceInstances.foldable(),
            listOf("a", "b", "c"), a -> monad.pure(a.length() > 1));

    assertAll(
        () -> assertEquals(true, match.value().fix(IOOf::narrowK).unsafeRunSync()),
        () -> assertEquals(false, notMatch.value().fix(IOOf::narrowK).unsafeRunSync())
    );
  }
}