package com.spdeveloper.chgc.genome.util.excel;

public class WrongExcelFormatException extends RuntimeException {
    public WrongExcelFormatException() {
        super();
    }
    public WrongExcelFormatException(String s) {
        super(s);
    }
    public WrongExcelFormatException(String s, Throwable throwable) {
        super(s, throwable);
    }
    public WrongExcelFormatException(Throwable throwable) {
        super(throwable);
    }
}
