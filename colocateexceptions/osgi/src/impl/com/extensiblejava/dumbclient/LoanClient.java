package com.extensiblejava.dumbclient;

import java.math.*;
import com.extensiblejava.facade.*;
import java.io.*;
import java.util.*;

public interface LoanClient {

	public void setLoanFacade(LoanFacade loanFacade);
	
	public void run() throws Exception;
	
	public void stop() throws Exception;
}