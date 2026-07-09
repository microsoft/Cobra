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

package com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.messages;

import com.nuance.plugin.nmsp.v1.nmspgw.protocol.nmsp.pdx.QueryResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PDXQueryResult extends PDXMessage implements QueryResult {
    private static Logger logger = LogManager.getLogger(PDXQueryResult.class);

    private final byte[] input;

    public PDXQueryResult(byte[] input) {
        super(PDXMessage.QueryResult, input);
        this.input = input;
    }

    public String getResultType() {
        String resultType = getUTF8String("result_type");
        if (resultType != null) {
            return resultType;
        } else {
            return "";
        }
    }

    public byte[] getRawInput() {
        return input;
    }

    public boolean isFinalResponse() {
        boolean finalResponse = true;
        try {
            if (containsKey("final_response")) {
                if (getInteger("final_response") == 0) {
                    finalResponse = false;
                }
                ;
            } else {
                logger.warn("Final response does not exist");
            }
        } catch (Exception e) {
            logger.warn("Failed to check for final response", e);
        }

        return finalResponse;
    }
}
