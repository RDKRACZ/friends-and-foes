package com.faboslav.friendsandfoes.client.render.entity;

import com.faboslav.friendsandfoes.FriendsAndFoes;
import com.faboslav.friendsandfoes.client.render.entity.feature.MoobloomFlowerFeatureRenderer;
import com.faboslav.friendsandfoes.entity.passive.MoobloomEntity;
import com.faboslav.friendsandfoes.init.ModEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
@SuppressWarnings({"rawtypes", "unchecked"})
public class MoobloomEntityRenderer extends MobEntityRenderer<MoobloomEntity, CowEntityModel<MoobloomEntity>>
{
	public MoobloomEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new CowEntityModel(context.getPart(ModEntityRenderer.MOOBLOOM_LAYER)), 0.7F);
		this.addFeature(new MoobloomFlowerFeatureRenderer(this));
	}

	@Override
	public Identifier getTexture(MoobloomEntity entity) {
		return FriendsAndFoes.makeID("textures/entity/moobloom/moobloom.png");
	}
}