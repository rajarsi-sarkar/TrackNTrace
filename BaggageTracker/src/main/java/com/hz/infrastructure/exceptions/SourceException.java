package com.hz.infrastructure.exceptions;

public class SourceException extends Exception{

    public SourceException(Exception aException){super(aException.getMessage());}
    public SourceException(String aMessage){super(aMessage);}
    public SourceException(String aMessage, Throwable aException){super(aMessage, aException);}
}
