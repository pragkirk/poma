package com.extensiblejava.route;

import com.extensiblejava.bill.*;

public class TypeARouter extends Router {

	public String route(Bill bill) {
		//route to location based on A routable type.
		return "A_LOCATION_";
	}

}