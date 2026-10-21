package com.tradepost.procure.admin.service;

import com.tradepost.procure.admin.SettingsBundle;
import com.tradepost.procure.admin.repo.SettingsRepository;
import com.tradepost.procure.common.Actors;
import com.tradepost.procure.common.InvalidRequestException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HexFormat;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class BundleService {

    private final SettingsRepository settings;
    private final SettingsService settingsService;

    public BundleService(SettingsRepository settings, SettingsService settingsService) {
        this.settings = settings;
        this.settingsService = settingsService;
    }

    public long store(String name, String blobHex) {
        return settings.insertBundle(name.trim(), blobHex);
    }

    public Map<String, Object> apply(long id) {
        byte[] bytes = HexFormat.of().parseHex(settings.bundleHex(id));
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            Object bundle = in.readObject();
            if (!(bundle instanceof SettingsBundle settingsBundle)) {
                throw new InvalidRequestException("the stored blob is not a settings bundle");
            }
            settingsBundle.entries().forEach((key, value) -> settingsService.put(key, value, Actors.current()));
            return Map.of("bundleId", id, "applied", settingsBundle.entries().size());
        } catch (IOException | ClassNotFoundException e) {
            throw new InvalidRequestException("the stored blob could not be read");
        }
    }
}
