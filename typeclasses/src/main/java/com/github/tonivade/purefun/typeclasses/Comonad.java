/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.typeclasses;

import static com.github.tonivade.purefun.Function1.identity;

import com.github.tonivade.purefun.Function1;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Witness;

public interface Comonad<F extends Witness> extends Functor<F> {

  <A, B> Kind<F, B> coflatMap(Kind<F, A> value, Function1<Kind<F, A>, B> map);

  <A> A extract(Kind<F, A> value);

  default <A> Kind<F, Kind<F, A>> coflatten(Kind<F, A> value) {
    return coflatMap(value, identity());
  }
}
