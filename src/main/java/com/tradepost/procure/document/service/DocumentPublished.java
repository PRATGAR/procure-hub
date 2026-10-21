package com.tradepost.procure.document.service;

import com.tradepost.procure.common.CommandResult;
import com.tradepost.procure.document.Document;
import java.util.concurrent.atomic.AtomicReference;

public record DocumentPublished(Document document, String format, String prefix,
                                AtomicReference<CommandResult> result) {
}
