/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.typeclasses;

import static com.github.tonivade.purefun.Nothing.nothing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;

import com.github.tonivade.purefun.Higher;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.Tuple;
import com.github.tonivade.purefun.Witness;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.monad.State;
import com.github.tonivade.purefun.monad.State;

public interface Console<W extends Witness> {
  
  Higher<W, String> readln();
  
  Higher<W, Nothing> println(String text);
  
  static Console<IO.µ> io() {
    return new ConsoleIO();
  }
  
  static Console<Higher<State.µ, ImmutableList<String>>> state() {
    return new ConsoleState();
  }
}

class ConsoleState implements Console<Higher<State.µ, ImmutableList<String>>> {

  @Override
  public State<ImmutableList<String>, String> readln() {
    return State.<ImmutableList<String>, String>state(list -> Tuple.of(list.tail(), list.head().get()));
  }

  @Override
  public State<ImmutableList<String>, Nothing> println(String text) {
    return State.<ImmutableList<String>, Nothing>state(list -> Tuple.of(list.append(text), nothing()));
  }
}

class ConsoleIO implements Console<IO.µ> {
  
  private final SystemConsole console = new SystemConsole();

  @Override
  public IO<String> readln() {
    return IO.of(console::readln);
  }

  @Override
  public IO<Nothing> println(String text) {
    return IO.exec(() -> console.println(text));
  }
}

final class SystemConsole {

  void println(String message) {
    writer().println(message);
  }

  String readln() {
    try {
      return reader().readLine();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
  
  private BufferedReader reader() {
    return new BufferedReader(new InputStreamReader(System.in));
  }
  
  private PrintWriter writer() {
    return new PrintWriter(System.out, true);
  }
}