package com.cerner.jwala.service.exception;

/**
 * Created by SP043299 on 9/6/2016.
 */
public class ZipDirectoryException extends RuntimeException {
    public ZipDirectoryException (final String s) {super (s);}

    public ZipDirectoryException (final Throwable t) {super (t);}

    public ZipDirectoryException (final String s, final Throwable t) {super (s, t);}
}
