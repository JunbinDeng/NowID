package com.nowid.sdk.exceptions

import COSE.MessageTag

/**
 * Exception thrown when an unsupported COSE message tag is encountered.
 *
 * @param tag The unsupported MessageTag that was encountered.
 */
class UnsupportedMessageTagException(
    tag: MessageTag
) : Exception("Unsupported COSE message tag: $tag")