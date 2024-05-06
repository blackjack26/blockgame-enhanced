package dev.jb0s.blockgameenhanced.gamefeature.statprofiles.allocate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.jb0s.blockgameenhanced.gamefeature.statprofiles.attribute.PlayerAttribute;

import java.util.*;
import java.util.function.Function;

public class StatProfile {
  public static final Codec<StatProfile> CODEC = RecordCodecBuilder.create(instance ->
      instance.group(
          Codec.STRING
              .fieldOf("name")
              .forGetter((StatProfile profile) -> profile.name),
          Codec.unboundedMap(Codec.STRING, Codec.INT)
              .xmap(HashMap::new, Function.identity())
              .fieldOf("attributeAllocations")
              .forGetter((StatProfile profile) -> profile.attributeAllocations)
      ).apply(instance, (name, attributeAllocations) -> {
        StatProfile profile = new StatProfile(name);
        profile.attributeAllocations.putAll(attributeAllocations);
        return profile;
      })
  );

  private final String name;
  private final HashMap<String, Integer> attributeAllocations;

  public StatProfile(String name) {
    this.name = name;
    this.attributeAllocations = new HashMap<>();
  }

  public void fromAttributes(Map<String, PlayerAttribute> attributes) {
    this.reset();

    for (Map.Entry<String, PlayerAttribute> entry : attributes.entrySet()) {
      int spent = entry.getValue().getSpent();
      if (spent > 0) {
        this.attributeAllocations.put(entry.getKey(), spent);
      }
    }
  }

  public Iterator<Command> buildCommands() {
    List<Command> commands = new ArrayList<>();
    commands.add(Command.reset());

    for (Map.Entry<String, Integer> entry : this.attributeAllocations.entrySet()) {
      for (int i = 0; i < entry.getValue(); i++) {
        commands.add(Command.increment(entry.getKey()));
      }
    }

    return commands.iterator();
  }

  public void reset() {
    this.attributeAllocations.clear();
  }

  public static StatProfile of(String name, Map<String, PlayerAttribute> attributes) {
    StatProfile profile = new StatProfile(name);
    profile.fromAttributes(attributes);
    return profile;
  }
}
