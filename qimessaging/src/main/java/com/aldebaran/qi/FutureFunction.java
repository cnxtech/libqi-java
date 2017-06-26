package com.aldebaran.qi;

/**
 * Interface used to provide a mechanism for chaining {@link Future}s by supplying
 * it to: {@link Future#then(FutureFunction)}.
 * <p>
 * For a simpler use, prefer using one of the two provided implementations:
 *
 * @param <T> the {@link Future}'s input type
 * @param <R> the {@link Future}'s result type
 */
public interface FutureFunction<T, R> {
    R execute(Future<T> future) throws Throwable;
}
