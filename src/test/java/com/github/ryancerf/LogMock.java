package com.github.ryancerf;

import org.apache.maven.plugin.logging.Log;

import java.util.ArrayList;
import java.util.List;

/** Fake log for integration tests. */
class LogMock implements Log {
  final List<String> debug = new ArrayList<>();
  final List<String> info = new ArrayList<>();
  final List<String> warning = new ArrayList<>();
  final List<String> error = new ArrayList<>();

  @Override
  public boolean isDebugEnabled() {
    return false;
  }

  @Override
  public void debug(CharSequence charSequence) {
    debug.add(charSequence.toString());
  }

  @Override
  public void debug(CharSequence charSequence, Throwable throwable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void debug(Throwable throwable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isInfoEnabled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void info(CharSequence charSequence) {
    this.info.add(charSequence.toString());
  }

  @Override
  public void info(CharSequence charSequence, Throwable throwable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void info(Throwable throwable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isWarnEnabled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void warn(CharSequence charSequence) {
    this.warning.add(charSequence.toString());
  }

  @Override
  public void warn(CharSequence charSequence, Throwable throwable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void warn(Throwable throwable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isErrorEnabled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void error(CharSequence charSequence) {
    this.error.add(charSequence.toString());
  }

  @Override
  public void error(CharSequence charSequence, Throwable throwable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void error(Throwable throwable) {
    throw new UnsupportedOperationException();
  }
}
