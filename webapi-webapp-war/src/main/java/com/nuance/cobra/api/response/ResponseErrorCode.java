package com.nuance.cobra.api.response;

import com.nuance.plugin.v1.shareable.IErrorCode;


/**
 * The methods have been moved to the IErrorCode base interface, which can be shared with plugins.
 * This interface is kept here to minimize other code changes.
 */
public interface ResponseErrorCode extends IErrorCode {
}
