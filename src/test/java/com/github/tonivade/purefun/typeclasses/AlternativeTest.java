/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.typeclasses;

import static com.github.tonivade.purefun.data.Sequence.listOf;
import static com.github.tonivade.purefun.typeclasses.AlternativeLaws.verifyLaws;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Option;

public class AlternativeTest {

  final Alternative<Sequence.µ> instance = Sequence.alternative();

  final Function1<Integer, Integer> twoTimes = a -> a * 2;
  final Function1<Integer, Integer> plusFive = a -> a + 5;

  @Test
  public void combineAndAp() {
    Higher1<Sequence.µ, Integer> seven = instance.pure(7);
    Higher1<Sequence.µ, Integer> eight = instance.pure(8);

    Higher1<Sequence.µ, Integer> result =
        instance.ap(instance.combineK(seven, eight),
                    instance.combineK(instance.pure(twoTimes), instance.pure(plusFive)));

    assertEquals(listOf(14, 16, 12, 13), result);
  }

  @Test
  public void sequence() {
    assertAll(() -> verifyLaws(Sequence.alternative()));
  }

  @Test
  public void option() {
    assertAll(() -> verifyLaws(Option.alternative()));
  }
}
