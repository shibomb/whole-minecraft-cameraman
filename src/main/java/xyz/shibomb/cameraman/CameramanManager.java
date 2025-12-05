package xyz.shibomb.cameraman;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import xyz.shibomb.cameraman.shots.CameraShot;
import xyz.shibomb.cameraman.shots.CraneShot;
import xyz.shibomb.cameraman.shots.DynamicFollowShot;
import xyz.shibomb.cameraman.shots.FlybyShot;
import xyz.shibomb.cameraman.shots.OrbitShot;
import xyz.shibomb.cameraman.shots.StaticShot;
import xyz.shibomb.cameraman.targets.CameraTarget;
import xyz.shibomb.cameraman.targets.EntityTarget;
import xyz.shibomb.cameraman.targets.LocationTarget;
import xyz.shibomb.cameraman.shots.MoveShot;

public class CameramanManager {

    private final JavaPlugin plugin;
    private UUID cameramanId;
    private boolean newcomerMode = false;
    private boolean rotationMode = false;
    private RotationTask rotationTask;
    private long rotationInterval = 200L; // Default 10 seconds (20 ticks * 10)
    private boolean mobTargetMode = false;
    private boolean autoMobTarget = false;
    private long autoMobTargetDelay = 5L; // Seconds
    private long lastPlayerTargetTime = System.currentTimeMillis();
    private boolean teleportSmooth = false;
    private long teleportSmoothDuration = 3L; // Seconds
    private SmoothTeleportTask currentTeleportTask;
    private boolean spectateMode = true;
    private SpectatePerspective spectatePerspective = SpectatePerspective.POV;
    private boolean mobSpectateMode = true;
    private SpectatePerspective mobSpectatePerspective = SpectatePerspective.POV;
    private SpectateTask currentSpectateTask;
    private boolean mobNightVision = false;
    private boolean showMessage = true;
    private int nightVisionThreshold = 7;
    private org.bukkit.potion.PotionEffect previousNightVisionEffect = null;
    private long previousNightVisionTime = 0;
    private BukkitTask lightCheckTask;
    private String spectateDistance = "3.0";
    private String spectateHeight = "1.0";
    private String orbitSpeed = "0.2";
    private String orbitDirection = "RANDOM";
    private String dynamicSmoothness = "0.1";
    private String flybyDuration = "30.0";
    private String craneDuration = "30.0";
    private String craneHeightMin = "1.0";
    private String craneHeightMax = "5.0";
    private List<SpectatePerspective> randomPlayerPerspectives = new ArrayList<>();
    private List<SpectatePerspective> randomMobPerspectives = new ArrayList<>();
    private List<SpectatePerspective> randomScenicPerspectives = new ArrayList<>();
    private boolean autoScenic = false;
    private SpectatePerspective autoScenicPerspective = SpectatePerspective.ORBIT;
    private String moveX = "-1-1";
    private String moveY = "0";
    private String moveZ = "-1-1";
    private String moveSpeed = "0.1";

    public CameramanManager(JavaPlugin plugin) {
        this.plugin = plugin;
        String uuidString = plugin.getConfig().getString("cameraman");
        if (uuidString != null) {
            try {
                this.cameramanId = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid cameraman UUID in config: " + uuidString);
            }
        }

        this.newcomerMode = plugin.getConfig().getBoolean("newcomerMode", false);
        this.rotationMode = plugin.getConfig().getBoolean("rotationMode", false);
        long intervalSeconds = plugin.getConfig().getLong("rotationInterval", 10L);
        this.rotationInterval = intervalSeconds * 20L;

        this.mobTargetMode = plugin.getConfig().getBoolean("mobTargetMode", false);
        this.autoMobTarget = plugin.getConfig().getBoolean("autoMobTarget", false);
        this.autoMobTargetDelay = plugin.getConfig().getLong("autoMobTargetDelay", 5L);

        this.teleportSmooth = plugin.getConfig().getBoolean("teleportSmooth", false);
        this.teleportSmoothDuration = plugin.getConfig().getLong("teleportSmoothDuration", 3L);

        this.spectateMode = plugin.getConfig().getBoolean("spectateMode", true);
        String perspectiveStr = plugin.getConfig().getString("spectatePerspective", "POV");
        try {
            this.spectatePerspective = SpectatePerspective.valueOf(perspectiveStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.spectatePerspective = SpectatePerspective.POV;
        }

        this.mobSpectateMode = plugin.getConfig().getBoolean("mobSpectateMode", true);
        String mobPerspectiveStr = plugin.getConfig().getString("mobSpectatePerspective", "POV");
        try {
            this.mobSpectatePerspective = SpectatePerspective.valueOf(mobPerspectiveStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.mobSpectatePerspective = SpectatePerspective.POV;
        }

        this.mobNightVision = plugin.getConfig().getBoolean("mobNightVision", false);
        this.showMessage = plugin.getConfig().getBoolean("showMessage", true);
        this.nightVisionThreshold = plugin.getConfig().getInt("nightVisionThreshold", 7);
        this.nightVisionThreshold = plugin.getConfig().getInt("nightVisionThreshold", 7);
        this.spectateDistance = plugin.getConfig().getString("spectateDistance", "3.0");
        this.spectateHeight = plugin.getConfig().getString("spectateHeight", "1.0");
        this.orbitSpeed = plugin.getConfig().getString("orbitSpeed", "0.2");
        this.orbitDirection = plugin.getConfig().getString("orbitDirection", "RANDOM");
        this.dynamicSmoothness = plugin.getConfig().getString("dynamicSmoothness", "0.1");
        this.flybyDuration = plugin.getConfig().getString("flybyDuration", "30.0");
        this.craneDuration = plugin.getConfig().getString("craneDuration", "30.0");
        this.craneHeightMin = plugin.getConfig().getString("craneHeightMin", "1.0");
        this.craneHeightMax = plugin.getConfig().getString("craneHeightMax", "5.0");
        this.moveX = plugin.getConfig().getString("moveX", "-1-1");
        this.moveY = plugin.getConfig().getString("moveY", "0");
        this.moveZ = plugin.getConfig().getString("moveZ", "-1-1");
        this.moveSpeed = plugin.getConfig().getString("moveSpeed", "0.1");

        this.autoScenic = plugin.getConfig().getBoolean("autoScenic", false);
        String autoScenicPerspStr = plugin.getConfig().getString("autoScenicPerspective", "ORBIT");
        try {
            this.autoScenicPerspective = SpectatePerspective.valueOf(autoScenicPerspStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.autoScenicPerspective = SpectatePerspective.ORBIT;
        }

        loadRandomPerspectives();

        checkAndStartRotationTask();
    }

    private void loadRandomPerspectives() {
        randomPlayerPerspectives = loadPerspectiveList("randomPlayerPerspectives");
        randomMobPerspectives = loadPerspectiveList("randomMobPerspectives");
        randomScenicPerspectives = loadPerspectiveList("randomScenicPerspectives");
    }

    private List<SpectatePerspective> loadPerspectiveList(String path) {
        List<String> list = plugin.getConfig().getStringList(path);
        if (list == null || list.isEmpty()) {
            // Default fallback if missing
            List<SpectatePerspective> defaults = new ArrayList<>();
            defaults.add(SpectatePerspective.POV);
            defaults.add(SpectatePerspective.BEHIND);
            defaults.add(SpectatePerspective.FRONT);
            defaults.add(SpectatePerspective.ORBIT);
            defaults.add(SpectatePerspective.DYNAMIC);
            defaults.add(SpectatePerspective.FLYBY);
            defaults.add(SpectatePerspective.CRANE);
            return defaults;
        }
        return list.stream().map(s -> {
            try {
                return SpectatePerspective.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid perspective in config: " + s);
                return null;
            }
        }).filter(p -> p != null && p != SpectatePerspective.RANDOM).collect(Collectors.toList());
    }

    private void checkAndStartRotationTask() {
        boolean shouldRun = rotationMode || mobTargetMode || autoMobTarget;

        // Always stop the existing task to ensure we use the latest interval/settings
        stopRotationTask();

        if (shouldRun) {
            this.rotationTask = new RotationTask(this);
            this.rotationTask.runTaskTimer(plugin, 0L, this.rotationInterval);
        }
    }

    public void setCameraman(Player player) {
        if (this.cameramanId != null && !this.cameramanId.equals(player.getUniqueId())) {
            Player oldCameraman = Bukkit.getPlayer(this.cameramanId);
            if (oldCameraman != null) {
                oldCameraman.setGameMode(GameMode.SURVIVAL);
                oldCameraman.sendMessage("You are no longer the cameraman.");
            }
        }
        this.cameramanId = player.getUniqueId();
        plugin.getConfig().set("cameraman", this.cameramanId.toString());
        plugin.saveConfig();
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage("You are now the cameraman!");
    }

    public Player getCameraman() {
        if (cameramanId == null)
            return null;
        return Bukkit.getPlayer(cameramanId);
    }

    public void restoreCameraman(Player player) {
        if (cameramanId != null && cameramanId.equals(player.getUniqueId())) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("Welcome back, Cameraman!");

        }
    }

    private void sendInfoMessage(Player player, String message) {
        if (showMessage && player != null) {
            player.sendMessage(message);
        }
    }

    public void setTarget(org.bukkit.entity.Entity target) {
        Player cameraman = getCameraman();
        if (cameraman != null && target != null) {
            // Clean up previous night vision states first
            stopLightCheckTask();
            stopSpectateTask();
            removeCameramanNightVision(cameraman);

            if (target instanceof Player) {
                if (cameraman.getUniqueId().equals(target.getUniqueId())) {
                    return; // Cannot spectate self
                }
                // If targeting a player, disable mob target mode (conceptually, we are focusing
                // on a player now)
                if (mobTargetMode) {
                    setMobTargetMode(false);
                    sendInfoMessage(cameraman, "Mob Target Mode disabled because a player was targeted.");
                }
                lastPlayerTargetTime = System.currentTimeMillis();
            }

            if (cameraman.getGameMode() != GameMode.SPECTATOR) {
                sendInfoMessage(cameraman,
                        "Cannot spectate " + target.getName() + " because you are not in Spectator mode.");
                return;
            }

            // Stop any existing smooth teleport task
            if (currentTeleportTask != null && !currentTeleportTask.isCancelled()) {
                currentTeleportTask.cancel();
            }

            cameraman.setSpectatorTarget(null); // Reset first

            // Determine active perspective
            SpectatePerspective configPerspective;
            if (target instanceof Player) {
                configPerspective = spectatePerspective;
            } else {
                configPerspective = mobSpectatePerspective;
            }

            SpectatePerspective activePerspective;
            if ("RANDOM".equalsIgnoreCase(configPerspective.name())) {
                List<SpectatePerspective> pool;
                if (target instanceof LocationTarget) {
                    pool = randomScenicPerspectives;
                } else if (target instanceof Player) {
                    pool = randomPlayerPerspectives;
                } else {
                    pool = randomMobPerspectives;
                }

                if (pool.isEmpty()) {
                    activePerspective = SpectatePerspective.POV;
                } else {
                    activePerspective = pool.get(new Random().nextInt(pool.size()));
                }
            } else {
                activePerspective = configPerspective;
            }

            double maxDist = parseAndCalculate(spectateDistance);
            double maxHeight = parseAndCalculate(spectateHeight);

            Runnable onTargetSet = () -> {
                boolean activeSpectateMode;
                if (target instanceof Player) {
                    activeSpectateMode = spectateMode;
                } else {
                    activeSpectateMode = mobSpectateMode;
                }

                if (activeSpectateMode) {
                    if (activePerspective == SpectatePerspective.POV) {
                        cameraman.setSpectatorTarget(target);
                        sendInfoMessage(cameraman, "Now spectating: " + target.getName() + " (POV)");
                    } else {
                        // For BEHIND/FRONT/ORBIT, we don't use setSpectatorTarget (it forces POV)
                        // Instead we start the positioning task
                        cameraman.setSpectatorTarget(null);
                        startSpectateTask(cameraman, target, activePerspective, maxDist, maxHeight);
                        sendInfoMessage(cameraman,
                                "Now spectating: " + target.getName() + " (" + activePerspective + ")");
                    }
                } else {
                    sendInfoMessage(cameraman, "Arrived at: " + target.getName());

                    // Auto Scenic Check (if not in spectate mode)
                    if (autoScenic) {
                        startScenicTask(target.getLocation(), autoScenicPerspective);
                    }
                }

                // Start Adaptive Night Vision check if applicable
                if (mobNightVision && target instanceof org.bukkit.entity.LivingEntity && !(target instanceof Player)) {
                    startLightCheckTask(cameraman, (org.bukkit.entity.LivingEntity) target);
                }
            };

            if (teleportSmooth) {
                sendInfoMessage(cameraman, "Moving to " + target.getName() + "...");
                CameraShot shot = createCameraShot(activePerspective, maxDist, maxHeight);
                currentTeleportTask = new SmoothTeleportTask(cameraman, new EntityTarget(target),
                        teleportSmoothDuration * 20L,
                        onTargetSet, shot);
                currentTeleportTask.runTaskTimer(plugin, 0L, 1L);
            } else {
                boolean activeSpectateMode = (target instanceof Player) ? spectateMode : mobSpectateMode;
                if (activeSpectateMode) {
                    onTargetSet.run();
                } else {
                    // Instant teleport with perspective
                    CameraShot shot = createCameraShot(activePerspective, maxDist, maxHeight);
                    Location targetLoc = shot.getNextLocation(cameraman, new EntityTarget(target), 0); // Get initial
                                                                                                       // position
                    cameraman.teleport(targetLoc);
                    sendInfoMessage(cameraman, "Moved to: " + target.getName());

                    // Auto Scenic Check (if not in spectate mode)
                    if (autoScenic) {
                        startScenicTask(target.getLocation(), autoScenicPerspective);
                    }

                    // Start Adaptive Night Vision check if applicable (Instant teleport case)
                    if (mobNightVision && target instanceof org.bukkit.entity.LivingEntity
                            && !(target instanceof Player)) {
                        startLightCheckTask(cameraman, (org.bukkit.entity.LivingEntity) target);
                    }
                }
            }
        }
    }

    public void clearTarget() {
        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.setSpectatorTarget(null);
            stopLightCheckTask();
            stopSpectateTask();
            removeCameramanNightVision(cameraman);
            sendInfoMessage(cameraman, "Stopped spectating.");
        }
    }

    private void startLightCheckTask(Player cameraman, org.bukkit.entity.LivingEntity target) {
        stopLightCheckTask();
        lightCheckTask = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (cameraman == null || !cameraman.isOnline() || target == null || !target.isValid()) {
                    this.cancel();
                    return;
                }

                int lightLevel = target.getLocation().getBlock().getLightLevel();
                if (lightLevel <= nightVisionThreshold) {
                    applyCameramanNightVision(cameraman);
                } else {
                    removeCameramanNightVision(cameraman);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Check every second
    }

    private void stopLightCheckTask() {
        if (lightCheckTask != null && !lightCheckTask.isCancelled()) {
            lightCheckTask.cancel();
            lightCheckTask = null;
        }
    }

    private void applyCameramanNightVision(Player player) {
        org.bukkit.potion.PotionEffect current = player
                .getPotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        // If current is null or not our infinite one (duration < 1000000 is a safe bet
        // for "not infinite")
        // We assume our infinite one is Integer.MAX_VALUE
        if (current != null && current.getDuration() < 1000000) {
            previousNightVisionEffect = current;
            previousNightVisionTime = System.currentTimeMillis();
        }

        // If we don't have a current effect, or we just saved it, apply ours
        if (current == null || current.getDuration() < 1000000) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION,
                    Integer.MAX_VALUE, 0, false, false));
        }
    }

    private void removeCameramanNightVision(Player player) {
        // First, check if we have the infinite effect. If so, remove it.
        org.bukkit.potion.PotionEffect current = player
                .getPotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        if (current != null && current.getDuration() > 1000000) {
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        }

        // Restore previous effect if it exists
        if (previousNightVisionEffect != null) {
            long elapsedTicks = (System.currentTimeMillis() - previousNightVisionTime) / 50;
            int remaining = previousNightVisionEffect.getDuration() - (int) elapsedTicks;

            if (remaining > 0) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        previousNightVisionEffect.getType(),
                        remaining,
                        previousNightVisionEffect.getAmplifier(),
                        previousNightVisionEffect.isAmbient(),
                        previousNightVisionEffect.hasParticles(),
                        previousNightVisionEffect.hasIcon()));
            }
            previousNightVisionEffect = null;
        }
    }

    public void setNewcomerMode(boolean enabled) {
        this.newcomerMode = enabled;
        plugin.getConfig().set("newcomerMode", enabled);
        plugin.saveConfig();
        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Newcomer mode set to: " + enabled);
        }
    }

    public boolean isNewcomerMode() {
        return newcomerMode;
    }

    public void setRotationMode(boolean enabled, long intervalSeconds) {
        this.rotationMode = enabled;

        plugin.getConfig().set("rotationMode", enabled);
        if (enabled) {
            plugin.getConfig().set("rotationInterval", intervalSeconds);
            this.rotationInterval = intervalSeconds * 20L;
        }
        plugin.saveConfig();

        checkAndStartRotationTask();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Rotation mode set to: " + enabled + " (Interval: " + intervalSeconds + "s)");
        }
    }

    public void stopRotationTask() {
        if (this.rotationTask != null && !this.rotationTask.isCancelled()) {
            this.rotationTask.cancel();
        }
    }

    public List<Player> getPotentialTargets() {
        List<Player> targets = new ArrayList<>(Bukkit.getOnlinePlayers());
        // Remove cameraman from targets
        if (cameramanId != null) {
            targets.removeIf(p -> p.getUniqueId().equals(cameramanId));
        }
        // Remove spectators
        targets.removeIf(p -> p.getGameMode() == GameMode.SPECTATOR);
        return targets;
    }

    public void setMobTargetMode(boolean enabled) {
        this.mobTargetMode = enabled;
        plugin.getConfig().set("mobTargetMode", enabled);
        plugin.saveConfig();

        checkAndStartRotationTask();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Mob Target Mode set to: " + enabled);
        }
    }

    public boolean isMobTargetMode() {
        return mobTargetMode;
    }

    public void setAutoMobTarget(boolean enabled, long delay) {
        this.autoMobTarget = enabled;
        this.autoMobTargetDelay = delay;
        plugin.getConfig().set("autoMobTarget", enabled);
        plugin.getConfig().set("autoMobTargetDelay", delay);
        plugin.saveConfig();

        checkAndStartRotationTask();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Auto Mob Target set to: " + enabled + " (Delay: " + delay + "s)");
        }
    }

    public boolean isAutoMobTarget() {
        return autoMobTarget;
    }

    public long getAutoMobTargetDelay() {
        return autoMobTargetDelay;
    }

    public long getLastPlayerTargetTime() {
        return lastPlayerTargetTime;
    }

    public List<org.bukkit.entity.LivingEntity> getPotentialMobTargets() {
        Player cameraman = getCameraman();
        List<org.bukkit.entity.LivingEntity> mobs = new ArrayList<>();
        if (cameraman != null) {
            for (org.bukkit.entity.Entity entity : cameraman.getNearbyEntities(50, 50, 50)) {
                if (entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player)
                        && !(entity instanceof org.bukkit.entity.ArmorStand)) {
                    mobs.add((org.bukkit.entity.LivingEntity) entity);
                }
            }
        }
        return mobs;
    }

    public void setTeleportSmooth(boolean enabled, long duration) {
        this.teleportSmooth = enabled;
        this.teleportSmoothDuration = duration;
        plugin.getConfig().set("teleportSmooth", enabled);
        plugin.getConfig().set("teleportSmoothDuration", duration);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Smooth Teleport set to: " + enabled + " (Duration: " + duration + "s)");
        }
    }

    public void setSpectateMode(boolean enabled) {
        this.spectateMode = enabled;
        plugin.getConfig().set("spectateMode", enabled);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Spectate Mode set to: " + enabled);
        }
    }

    public void setSpectatePerspective(SpectatePerspective perspective) {
        this.spectatePerspective = perspective;
        plugin.getConfig().set("spectatePerspective", perspective.name());
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Spectate Perspective (Player) set to: " + perspective);
        }
    }

    public void setMobSpectateMode(boolean enabled) {
        this.mobSpectateMode = enabled;
        plugin.getConfig().set("mobSpectateMode", enabled);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Spectate Mode (Mob) set to: " + enabled);
        }
    }

    public void setMobSpectatePerspective(SpectatePerspective perspective) {
        this.mobSpectatePerspective = perspective;
        plugin.getConfig().set("mobSpectatePerspective", perspective.name());
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Spectate Perspective (Mob) set to: " + perspective);
        }
    }

    private void startSpectateTask(Player cameraman, org.bukkit.entity.Entity target, SpectatePerspective perspective,
            double distance,
            double height) {
        if (target == null)
            return;

        CameraShot shot = createCameraShot(perspective, distance, height);

        // Cancel previous task
        if (currentSpectateTask != null && !currentSpectateTask.isCancelled()) {
            currentSpectateTask.cancel();
        }

        currentSpectateTask = new SpectateTask(cameraman, new EntityTarget(target), shot);
        currentSpectateTask.runTaskTimer(plugin, 0L, 1L);
    }

    public void startScenicTask(Location startLocation, SpectatePerspective perspective) {
        Player cameraman = getCameraman();
        if (cameraman == null)
            return;

        // Ensure Spectator mode
        if (cameraman.getGameMode() != GameMode.SPECTATOR) {
            cameraman.sendMessage("You must be in Spectator Mode to use Scenic Mode.");
            return;
        }

        // Resolve RANDOM if needed
        SpectatePerspective actualPerspective = perspective;
        if (perspective == SpectatePerspective.RANDOM) {
            if (!randomScenicPerspectives.isEmpty()) {
                actualPerspective = randomScenicPerspectives
                        .get(new Random().nextInt(randomScenicPerspectives.size()));
            } else {
                actualPerspective = SpectatePerspective.ORBIT; // Default fallback
            }
        }

        // Parse configs directly or reuse existing fields?
        // We can reuse the fields.
        double distance = parseAndCalculate(spectateDistance);
        double height = parseAndCalculate(spectateHeight);

        CameraShot shot = createCameraShot(actualPerspective, distance, height);

        // Stop other tasks
        stopFollowing();

        currentSpectateTask = new SpectateTask(cameraman, new LocationTarget(startLocation), shot);
        // Execute immediately to prevent 1-tick jitter
        currentSpectateTask.run();
        // Schedule subsequent runs starting from next tick
        currentSpectateTask.runTaskTimer(plugin, 1L, 1L);
    }

    public void stopFollowing() {
        if (currentSpectateTask != null && !currentSpectateTask.isCancelled()) {
            currentSpectateTask.cancel();
        }
        if (currentTeleportTask != null && !currentTeleportTask.isCancelled()) {
            currentTeleportTask.cancel();
            currentTeleportTask = null;
        }
        Player p = getCameraman();
        if (p != null)
            p.setSpectatorTarget(null);
    }

    private CameraShot createCameraShot(SpectatePerspective perspective, double distance, double height) {
        if (perspective == SpectatePerspective.ORBIT) {
            double speed = parseAndCalculate(orbitSpeed);
            boolean isRight = false;

            if ("RANDOM".equalsIgnoreCase(orbitDirection)) {
                isRight = new Random().nextBoolean();
            } else if ("RIGHT".equalsIgnoreCase(orbitDirection)) {
                isRight = true;
            }

            if (isRight) {
                speed = -speed; // Negative speed for RIGHT orbit (assuming positive is LEFT/CCW)
            }

            return new OrbitShot(distance, speed, height);
        } else if (perspective == SpectatePerspective.DYNAMIC) {
            double smooth = Double.parseDouble(dynamicSmoothness);
            return new DynamicFollowShot(distance, height, smooth);
        } else if (perspective == SpectatePerspective.FLYBY) {
            double dur = Double.parseDouble(flybyDuration);
            return new FlybyShot(dur);
        } else if (perspective == SpectatePerspective.CRANE) {
            double dur = Double.parseDouble(craneDuration);
            double min = Double.parseDouble(craneHeightMin);
            double max = Double.parseDouble(craneHeightMax);
            return new CraneShot(dur, min, max, distance);
        } else if (perspective == SpectatePerspective.MOVE) {
            double x = parseAndCalculate(moveX);
            double y = parseAndCalculate(moveY);
            double z = parseAndCalculate(moveZ);
            double speed = parseAndCalculate(moveSpeed);
            return new MoveShot(x, y, z, speed, distance, height);
        } else {
            return new StaticShot(perspective, distance, height);
        }
    }

    private void stopSpectateTask() {
        if (currentSpectateTask != null && !currentSpectateTask.isCancelled()) {
            currentSpectateTask.cancel();
            currentSpectateTask = null;
        }
    }

    public void setMobNightVision(boolean enabled) {
        this.mobNightVision = enabled;
        plugin.getConfig().set("mobNightVision", enabled);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Mob Night Vision set to: " + enabled);
        }
    }

    public void setShowMessage(boolean enabled) {
        this.showMessage = enabled;
        plugin.getConfig().set("showMessage", enabled);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Show Message set to: " + enabled);
        }
    }

    public void setNightVisionThreshold(int threshold) {
        this.nightVisionThreshold = threshold;
        plugin.getConfig().set("nightVisionThreshold", threshold);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Night Vision Threshold set to: " + threshold);
        }
    }

    public void setSpectateDistance(String distance) {
        this.spectateDistance = distance;
        plugin.getConfig().set("spectateDistance", distance);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Spectate Distance set to: " + distance);
        }
    }

    public void setSpectateHeight(String height) {
        this.spectateHeight = height;
        plugin.getConfig().set("spectateHeight", height);
        plugin.saveConfig();

        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Spectate Height set to: " + height);
        }
    }

    private double parseAndCalculate(String value) {
        if (value == null)
            return 3.0; // Fallback matches default logic
        try {
            // Regex for range: (number)-(number)
            // Supports negative numbers e.g. -1.5--0.5, -5-5
            // Group 1: Min, Group 3: Max (skipping inner groups for decimals)
            java.util.regex.Pattern rangePattern = java.util.regex.Pattern
                    .compile("^((-?\\d+(\\.\\d+)?))-((-?\\d+(\\.\\d+)?))$");
            java.util.regex.Matcher matcher = rangePattern.matcher(value.trim());

            if (matcher.matches()) {
                double min = Double.parseDouble(matcher.group(1));
                double max = Double.parseDouble(matcher.group(4));

                if (min > max) {
                    double temp = min;
                    min = max;
                    max = temp;
                }
                return min + (new Random().nextDouble() * (max - min));
            }

            // Not a range, try parsing as single value
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // Log warning?
            // plugin.getLogger().warning("Failed to parse value: " + value);
            return 3.0; // Fallback default
        }
    }

    public void setOrbitSpeed(String speed) {
        this.orbitSpeed = speed;
        plugin.getConfig().set("orbitSpeed", speed);
        plugin.saveConfig();
        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Orbit Speed set to: " + speed);
        }
    }

    public void setOrbitDirection(String direction) {
        this.orbitDirection = direction;
        plugin.getConfig().set("orbitDirection", direction);
        plugin.saveConfig();
        Player cameraman = getCameraman();
        if (cameraman != null) {
            cameraman.sendMessage("Orbit Direction set to: " + direction);
        }
    }

    // Setters for new configs could go here, omitting for brevity/task balance
    // unless requested.
    // Actually, I'll add them to support the commands I planned.

    public void setDynamicSmoothness(String val) {
        this.dynamicSmoothness = val;
        plugin.getConfig().set("dynamicSmoothness", val);
        plugin.saveConfig();
        if (getCameraman() != null)
            getCameraman().sendMessage("Dynamic Smoothness: " + val);
    }

    public void setFlybyDuration(String val) {
        this.flybyDuration = val;
        plugin.getConfig().set("flybyDuration", val);
        plugin.saveConfig();
        if (getCameraman() != null)
            getCameraman().sendMessage("Flyby Duration: " + val);
    }

    public void setCraneDuration(String val) {
        this.craneDuration = val;
        plugin.getConfig().set("craneDuration", val);
        plugin.saveConfig();
        if (getCameraman() != null)
            getCameraman().sendMessage("Crane Duration: " + val);
    }

    public void setRandomPerspectives(String type, List<String> perspectives) {
        String path;
        List<SpectatePerspective> targetList;
        if (type.equalsIgnoreCase("player")) {
            path = "randomPlayerPerspectives";
        } else if (type.equalsIgnoreCase("mob")) {
            path = "randomMobPerspectives";
        } else {
            path = "randomScenicPerspectives";
        }

        plugin.getConfig().set(path, perspectives);
        plugin.saveConfig();
        loadRandomPerspectives(); // Reload

        if (getCameraman() != null) {
            getCameraman().sendMessage(
                    "Random perspectives updated for " + type + ": " + perspectives);
        }
    }

    public void setMoveSpeed(String speed) {
        this.moveSpeed = speed;
        plugin.getConfig().set("moveSpeed", speed);
        plugin.saveConfig();
        if (getCameraman() != null) {
            getCameraman().sendMessage("Move Speed set to: " + speed);
        }
    }

    public void setMoveDirection(String x, String y, String z) {
        this.moveX = x;
        this.moveY = y;
        this.moveZ = z;
        plugin.getConfig().set("moveX", x);
        plugin.getConfig().set("moveY", y);
        plugin.getConfig().set("moveZ", z);
        plugin.saveConfig();
        if (getCameraman() != null) {
            getCameraman().sendMessage("Move Direction set to: X=" + x + ", Y=" + y + ", Z=" + z);
        }
    }

    public void setAutoScenic(boolean enabled, SpectatePerspective perspective) {
        this.autoScenic = enabled;
        if (perspective != null) {
            this.autoScenicPerspective = perspective;
            plugin.getConfig().set("autoScenicPerspective", perspective.name());
        }
        plugin.getConfig().set("autoScenic", enabled);
        plugin.saveConfig();

        if (getCameraman() != null) {
            String msg = "Auto Scenic: " + enabled;
            if (perspective != null) {
                msg += " (Perspective: " + perspective.name() + ")";
            }
            getCameraman().sendMessage(msg);
        }
    }
}
