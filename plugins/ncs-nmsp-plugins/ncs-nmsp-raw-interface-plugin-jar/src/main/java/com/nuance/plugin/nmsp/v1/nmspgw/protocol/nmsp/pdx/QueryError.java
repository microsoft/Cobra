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
 * Represents an error message sent from 
 * Nuance Mobile Application Server to the client application to access the <code>QueryError</code>
 * content.
 *
 * @author Nuance Communication Inc.
 */
public interface QueryError {
    /**
     * Retrieves the error code.
     *
     * @return <code>int</code> errorCode
     */
    public int getError();

    /**
     * Retrieves the error description.
     *
     * @return <code>String</code> description of the error
     */
    public String getDescription();
}
