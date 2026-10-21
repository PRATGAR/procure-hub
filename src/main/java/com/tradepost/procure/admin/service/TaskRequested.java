package com.tradepost.procure.admin.service;

import java.util.concurrent.atomic.AtomicInteger;

public record TaskRequested(String name, String cutoff, AtomicInteger affected) {
}
