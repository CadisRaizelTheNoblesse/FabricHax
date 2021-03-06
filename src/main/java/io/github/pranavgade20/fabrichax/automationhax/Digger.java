package io.github.pranavgade20.fabrichax.automationhax;

import io.github.pranavgade20.fabrichax.Settings;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Digger extends AutomationBase {
    public static int up = 3;
    public static int down = 1;
    public static int left = 3;
    public static int right = 3;

    public static Digger INSTANCE;
    public Digger() {
        INSTANCE = this;
    }

    public static boolean dig(ItemStack itemStack, World world, BlockPos blockPos, Direction direction) {
        for (int i = -left; i <= right; i++) {
            for (int j = -down; j <= up; j++) {
                switch (direction) {
                    case UP:
                    case DOWN:
                        return false; //not implemented because ppl are stupid
                    case EAST:
                    case WEST:
                        BlockPos block = blockPos.add(0, j, i);
                        if (getBlockBreakingSpeed(itemStack, world.getBlockState(block)) >= 34f) {
                            ClientSidePacketRegistry.INSTANCE.sendToServer(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, block, direction));
                            ClientSidePacketRegistry.INSTANCE.sendToServer(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, block, direction));

                            world.removeBlock(block, false);
                        }
                        break;
                    case NORTH:
                    case SOUTH:
                        BlockPos blk = blockPos.add(i, j, 0);
                        if (getBlockBreakingSpeed(itemStack, world.getBlockState(blk)) >= 34f) {
                            ClientSidePacketRegistry.INSTANCE.sendToServer(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blk, direction));
                            ClientSidePacketRegistry.INSTANCE.sendToServer(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blk, direction));

                            world.removeBlock(blk, false);
                        }
                        break;
                }
            }
        }
        return getBlockBreakingSpeed(itemStack, world.getBlockState(blockPos)) >= 34f;
    }

    public static float getBlockBreakingSpeed(ItemStack tool, BlockState block) { // adapted from net.minecraft.entity.player.PlayerEntity.class
        float f = tool.getMiningSpeedMultiplier(block);

        if (f > 1.0F) {
            int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, tool);

            if (efficiency > 0 && !tool.isEmpty()) {
                f += (float) (efficiency * efficiency + 1);
            }
        }

        return f;
    }

    @Override
    public String getHelpMessage() {
        return "Digger - mine large chunks of blocks quickly.\n" +
                "You must be able to insta-mine the blocks for best results\n" +
                "(for example, you need an efficiency 5 shovel to use Digger on sand)\n" +
                "\nConfiguration information:\n" +
                " ~ config Digger <direction> <size>\n" +
                " (to configure shape to be dug out)\n" +
                " where directions include 'up, down, left, right'\n" +
                " and size is a non negative number" +
                " for example, use `config Digger 2 left`\n" +
                " to set this to mine 2 blocks to the left of your selected block.\n" +
                " \n" +
                " note: if you are kicked, try selecting a lower number for dig size";
    }

    @Override
    public String getToolTip() {
        return "Mine large chunks of blocks at a time";
    }

    @Override
    public void config(String params) {
        try {
            String direction = params.split(" ")[1].toLowerCase();
            int size = Integer.parseInt(params.split(" ")[2]);

            switch (direction) {
                case "up":
                    up = size;
                    break;
                case "down":
                    down = size;
                    break;
                case "left":
                    left = size;
                    break;
                case "right":
                    right = size;
                    break;
                default:
                    Settings.player.sendMessage(Text.of("Invalid use: refer to help(~ help digger) for more information."), false);
                    return;
            }
            Settings.player.sendMessage(Text.of("~ config Digger " + params), false);
        } catch (Exception e) {
            Settings.player.sendMessage(Text.of("Invalid use: refer to help(~ help digger) for more information."), false);
        }
    }

    @Override
    public Screen getConfigScreen(Screen parent, String name) {
        return new Screen(Text.of(name)) {
            @Override
            public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                this.renderBackground(matrices);
                drawCenteredText(matrices, this.textRenderer, getTitle(), this.width / 2, 10, 16777215);
                super.render(matrices, mouseX, mouseY, delta);
            }

            @Override
            public void onClose() {
                MinecraftClient.getInstance().openScreen(parent);
            }

            @Override
            protected void init() {
                int x = 10;
                int y = 30;
                addButton(new TextFieldWidget(this.textRenderer, x, y, 100, 20, Text.of("Enabled")) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        int j = this.active ? 16777215 : 10526880;
                        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
                    }
                });
                addButton(new AbstractButtonWidget(x+110, y, 100, 20, Text.of(String.valueOf(enabled))) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        enabled = !enabled;
                        setMessage(Text.of(String.valueOf(enabled)));
                    }
                });
                y+=25;

                addButton(new TextFieldWidget(textRenderer, x, y, 100, 20, Text.of("Up")) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        int j = this.active ? 16777215 : 10526880;
                        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
                    }
                });
                final TextFieldWidget up = addButton(new TextFieldWidget(textRenderer, x+110+25, y, 50, 20, Text.of(String.valueOf(Digger.up))) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        int j = this.active ? 16777215 : 10526880;
                        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
                    }
                });
                addButton(new AbstractButtonWidget(x+110, y, 20, 20, Text.of("-")) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        Digger.up = Digger.up == 0 ? 0 : Digger.up-1;
                        up.setMessage(Text.of(String.valueOf(Digger.up)));
                    }
                });
                addButton(new AbstractButtonWidget(x+110+25+55, y, 20, 20, Text.of("+")) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        Digger.up = Digger.up == 8 ? 8 : Digger.up+1;
                        up.setMessage(Text.of(String.valueOf(Digger.up)));
                    }
                });
                y+=25;

                addButton(new TextFieldWidget(textRenderer, x, y, 100, 20, Text.of("Down")) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        int j = this.active ? 16777215 : 10526880;
                        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
                    }
                });
                final TextFieldWidget down = addButton(new TextFieldWidget(textRenderer, x+110+25, y, 50, 20, Text.of(String.valueOf(Digger.down))) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        int j = this.active ? 16777215 : 10526880;
                        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
                    }
                });
                addButton(new AbstractButtonWidget(x+110, y, 20, 20, Text.of("-")) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        Digger.down = Digger.down == 0 ? 0 : Digger.down-1;
                        down.setMessage(Text.of(String.valueOf(Digger.down)));
                    }
                });
                addButton(new AbstractButtonWidget(x+110+25+55, y, 20, 20, Text.of("+")) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        Digger.down = Digger.down == 8 ? 8 : Digger.down+1;
                        down.setMessage(Text.of(String.valueOf(Digger.down)));
                    }
                });
                y+=25;

                addButton(new TextFieldWidget(textRenderer, x, y, 100, 20, Text.of("Left")) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        int j = this.active ? 16777215 : 10526880;
                        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
                    }
                });
                final TextFieldWidget left = addButton(new TextFieldWidget(textRenderer, x+110+25, y, 50, 20, Text.of(String.valueOf(Digger.left))) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        int j = this.active ? 16777215 : 10526880;
                        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
                    }
                });
                addButton(new AbstractButtonWidget(x+110, y, 20, 20, Text.of("-")) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        Digger.left = Digger.left == 0 ? 0 : Digger.left-1;
                        left.setMessage(Text.of(String.valueOf(Digger.left)));
                    }
                });
                addButton(new AbstractButtonWidget(x+110+25+55, y, 20, 20, Text.of("+")) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        Digger.left = Digger.left == 8 ? 8 : Digger.left+1;
                        left.setMessage(Text.of(String.valueOf(Digger.left)));
                    }
                });
                y+=25;

                addButton(new TextFieldWidget(textRenderer, x, y, 100, 20, Text.of("Right")) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        int j = this.active ? 16777215 : 10526880;
                        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
                    }
                });
                final TextFieldWidget right = addButton(new TextFieldWidget(textRenderer, x+110+25, y, 50, 20, Text.of(String.valueOf(Digger.right))) {
                    @Override
                    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                        int j = this.active ? 16777215 : 10526880;
                        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
                    }
                });
                addButton(new AbstractButtonWidget(x+110, y, 20, 20, Text.of("-")) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        Digger.right = Digger.right == 0 ? 0 : Digger.right-1;
                        right.setMessage(Text.of(String.valueOf(Digger.right)));
                    }
                });
                addButton(new AbstractButtonWidget(x+110+25+55, y, 20, 20, Text.of("+")) {
                    @Override
                    public void onClick(double mouseX, double mouseY) {
                        Digger.right = Digger.right == 8 ? 8 : Digger.right+1;
                        right.setMessage(Text.of(String.valueOf(Digger.right)));
                    }
                });
            }
        };
    }
}
