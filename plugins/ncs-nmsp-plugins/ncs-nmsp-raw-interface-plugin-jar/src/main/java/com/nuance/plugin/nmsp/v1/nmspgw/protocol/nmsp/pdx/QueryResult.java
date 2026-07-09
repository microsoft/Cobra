/**
 * Copyright 2006-2011, Nuance Communications Inc. All rights reserved.
 * <p>
 * Nuance Communications, Inc. provides this document without representation
 * or warranty of any kind. The information in this document is subject to
 * change without notice and does not represent a commitment by Nuance
 * Communications, Inc. The software and/or databases described in this
 * document are furnished under a license agreement and may be used or
 * copied only in accordance with the terms of such license agreement.
 * Without limiting the rights under copyright reserved herein, and except
 * as permitted by such license agreement, no part of this document may be
 * reproduced or transmitted in any form or by any means, including, without
 * limitation, electronic, mechanical, photocopying, recording, or otherwise,
 * or transferred to information storage and retrieval systems, without the
 * prior written permission of Nuance Communications, Inc.
 * <p>
 * Nuance, the Nuance logo, Nuance Recognizer, and Nuance Vocalizer are
 * trademarks or registered trademarks of Nuance Communications, Inc. or its
 * affiliates in the United States and/or other countries. All other
 * trademarks referenced herein are the property of their respective owners.
 **/

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.types.Dictionary;

/**
 * Represents results returned from 
 * Nuance Mobile Application Server to the client application. 
 * Also provides access to <code>QueryResult</code> content.
 * Inherits from the <code>Dictionary</code> interface, 
 * which you can use to retrieve the content
 * of the <code>QueryResult</code>.
 *
 * @author Nuance Communication Inc.
 */
public interface QueryResult extends Dictionary {
    /**
     * Retrieves the result type (command name).
     *
     * @return <code>String</code> result type
     */
    public String getResultType();

    /**
     * Retrieves the raw data that was used to create the result.
     *
     * @return Raw data
     */
    byte[] getRawInput();

    /**
     * Retrieves if the result is the final response. This method should
     * only be called when intermediate response feature has been enabled
     * @return true, if the result is final response;
     *         false, if there are follow-up responses.
     */
    public boolean isFinalResponse();
}
