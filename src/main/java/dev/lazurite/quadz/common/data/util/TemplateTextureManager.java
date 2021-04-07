package dev.lazurite.quadz.common.data.util;

import dev.lazurite.quadz.common.data.DataDriver;
import dev.lazurite.quadz.common.data.model.Template;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TemplateTextureManager implements ResourceManager {
    @Override
    public Set<String> getAllNamespaces() {
        return new HashSet<>();
    }

    @Override
    public Resource getResource(Identifier id) {
        Template template = DataDriver.getTemplate(id.getPath());
        return new ResourceImpl(template.getId(), id, new ByteArrayInputStream(template.getTexture()), null);
    }

    @Override
    public boolean containsResource(Identifier id) {
        return false;
    }

    @Override
    public List<Resource> getAllResources(Identifier id) throws IOException {
        return new ArrayList<>();
    }

    @Override
    public Collection<Identifier> findResources(String resourceType, Predicate<String> pathPredicate) {
        return new ArrayList<>();
    }

    @Override
    public Stream<ResourcePack> streamResourcePacks() {
        return null;
    }
}