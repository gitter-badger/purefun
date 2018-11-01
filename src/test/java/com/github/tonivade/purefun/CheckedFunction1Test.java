/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun;

import static com.github.tonivade.purefun.CheckedFunction1.failure;
import static com.github.tonivade.purefun.Nothing.nothing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;

public class CheckedFunction1Test {

  private final CheckedFunction1<String, String> identity = CheckedFunction1.identity();
  private final CheckedFunction1<String, Integer> str2int = string -> string.length();
  private final CheckedFunction1<Integer, String> int2str = integer -> String.valueOf(integer);

  @Test
  public void andThenTest() throws Exception {
    String result = str2int.andThen(int2str).apply("asdfg");

    assertEquals("5", result);
  }

  @Test
  public void composeTest() throws Exception {
    String result = int2str.compose(str2int).apply("asdfg");

    assertEquals("5", result);
  }

  @Test
  public void identityTest() throws Exception {
    String result = identity.apply("5");

    assertEquals("5", result);
  }

  @Test
  public void recoverTest() {
    Integer result = str2int.unchecked().apply("asdfg");

    assertEquals(Integer.valueOf(5), result);
  }

  @Test
  public void failureTest() {
    CheckedFunction1<Nothing, Nothing> failure = failure(Exception::new);

    assertThrows(Exception.class, () -> failure.unchecked().apply(nothing()));
  }

  @Test
  public void liftEitherTest() {
    Either<Throwable, Integer> result = str2int.liftEither().apply("asdfg");

    assertEquals(Either.right(5), result);
  }

  @Test
  public void liftOptionTest() {
    Option<Integer> result = str2int.liftOption().apply("asdfg");

    assertEquals(Option.some(5), result);
  }

  @Test
  public void liftTrySuccessTest() {
    Try<Integer> result = str2int.liftTry().apply("asdfg");

    assertEquals(Try.success(5), result);
  }

  @Test
  public void liftTryFailureTest() {
    Try<Nothing> result =
        CheckedFunction1.<Nothing, Exception>failure(Exception::new).liftTry().apply(nothing());

    assertTrue(result.isFailure());
  }
}
