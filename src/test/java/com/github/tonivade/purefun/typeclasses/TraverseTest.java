/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.typeclasses;

import static com.github.tonivade.purefun.Nested.nest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.Nested;
import com.github.tonivade.purefun.instances.IdInstances;
import com.github.tonivade.purefun.instances.OptionInstances;
import com.github.tonivade.purefun.instances.TryInstances;
import com.github.tonivade.purefun.type.Id;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;

public class TraverseTest {

  @Test
  public void composed() {
    Traverse<Nested<Option.µ, Id.µ>> composed = Traverse.compose(OptionInstances.traverse(), IdInstances.traverse());

    assertEquals(Try.success(Option.some(Id.of("HOLA!"))),
        composed.traverse(TryInstances.applicative(), nest(Option.some(Id.of(Try.success("hola!")))),
            t -> t.map(String::toUpperCase)));
  }

  @Test
  public void sequence() {
    Traverse<Nested<Option.µ, Id.µ>> composed = Traverse.compose(OptionInstances.traverse(), IdInstances.traverse());

    assertEquals(Try.success(Option.some(Id.of("hola!"))),
        composed.sequence(TryInstances.applicative(), nest(Option.some(Id.of(Try.success("hola!"))))));
  }
}
