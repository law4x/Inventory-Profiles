package io.github.jsnimda.inventoryprofiles.mixin;

import io.github.jsnimda.inventoryprofiles.gui.inject.ContainerScreenEventHandler;
import io.github.jsnimda.inventoryprofiles.gui.inject.ScreenEventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinAbstractContainerScreen
 */
@Mixin(HandledScreen.class)
public abstract class MixinContainerScreen<T extends ScreenHandler> extends Screen {

  protected MixinContainerScreen(Text text) {
    super(text);
  }

//  @Inject(at = @At("RETURN"), method = "init()V")
//  protected void init(CallbackInfo info) {
//    List<InjectWidget> list = ContainerScreenHandler.INSTANCE.getContainerInjector((HandledScreen) (Object) this);
//    for (InjectWidget iw : list) {
//      addButton(iw);
//    }
//  }

//  @Inject(at = @At("RETURN"), method = "render(IIF)V")
//  public void render(int int_1, int int_2, float float_1, CallbackInfo info) {
//    Tooltips.INSTANCE.renderAll();
//  }

  @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;" +
      "drawBackground(Lnet/minecraft/client/util/math/MatrixStack;FII)V", shift = At.Shift.AFTER), method = "render")
  public void onBackgroundRender(MatrixStack matrixStack, int i, int j, float f, CallbackInfo ci) {
    ContainerScreenEventHandler.INSTANCE.onBackgroundRender();
  }

  @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;" +
      "drawForeground(Lnet/minecraft/client/util/math/MatrixStack;II)V", shift = At.Shift.AFTER), method = "render")
  public void onForegroundRender(MatrixStack matrixStack, int i, int j, float f, CallbackInfo ci) {
    ContainerScreenEventHandler.INSTANCE.onForegroundRender();
  }
}