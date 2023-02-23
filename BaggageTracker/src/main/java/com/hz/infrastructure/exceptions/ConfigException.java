package com.hz.infrastructure.exceptions;

public class ConfigException extends Exception{

    public ConfigException(Exception aException){super(aException.getMessage());}
    public ConfigException(String aMessage){super(aMessage);}
    public ConfigException(String aMessage, Throwable aException){super(aMessage, aException);}
}
