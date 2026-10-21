package com.tradepost.procure.document.service;

import com.tradepost.procure.common.CommandResult;
import com.tradepost.procure.document.Document;

public interface DocumentConverter {

    CommandResult convert(Document document, String prefix);
}
