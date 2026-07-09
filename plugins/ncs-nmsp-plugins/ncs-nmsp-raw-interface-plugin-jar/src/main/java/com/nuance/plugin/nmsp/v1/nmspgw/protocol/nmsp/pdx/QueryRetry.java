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

/**
 * Indicates to the client application that Nuance Mobile
 * Application Server did not produce a proper recognition result or that the
 * confidence score was too low.
 * Prompts the client to resend one or more parameters. 
 * The name of the parameter to resend is contained as a value in the <code>QueryRetry</code>
 * parameter.
 *
 * @author Nuance Communication Inc.
 */
public interface QueryRetry {
    /**
     * Retrieves the parameter name to retry.
     *
     * @return <code>String</code> name
     */
    public String getName();

    /**
     * Retrieves the cause of the retry. If the cause is unknown, returns zero. 
     *
     * @return int cause
     */
    public int getCause();

    /**
     * Retrieves the prompt to play. Is empty if cause is unknown. 
     *
     * @return String prompt
     */
    public String getPrompt();
}
