# Rendering & Asset Migration Review (Forge 1.20.1)

Goal: explain why many inventory icons/models are missing or blocks fail to render/place by comparing the current 1.20.1 port with the 1.7.10 code and Forge 1.20.1 docs.

## High-Impact Findings
- **Descriptor items lost their icon pipeline**: `src/main/kotlin/mods/eln/generic/GenericItemUsingDamageDescriptor.kt` now registers plain `Item` instances with no `IClientItemExtensions` override or item models. In 1.7.10, `old_mod/ElectricalAge-1.22.x/src/main/kotlin/mods/eln/generic/GenericItemUsingDamageDescriptor.kt` implemented `updateIcons`/`renderItem` to pull `iconName` textures. Without JSON models or a custom renderer, Forge renders the pink/black missing icon for every descriptor-based item.
- **Item textures folder not ported**: the old mod had `old_mod/ElectricalAge-1.22.x/src/main/resources/assets/eln/textures/items/*`, but the 1.20.1 resources do not contain `textures/items`. Even if we generate models, they have no textures to point at.
- **Block items rely on models referencing absent textures**: e.g., `src/main/resources/assets/eln/models/block/energy_converter.json` points to `eln:block/energy_converter`, but there is no matching texture under `src/main/resources/assets/eln/textures/block`. Similar risk exists for any model that references textures not present or still named with legacy casing.
- **Ghost block has no client assets**: `Registration.GHOST_BLOCK` is registered, but there is no `blockstates/ghost_block.json`, no block model, and no item model. The engine will report missing models and render it as magenta.
- **Creative tab contents hide descriptor items when models are missing**: `Registration.ELN_TAB` iterates `ITEMS.entries`, but descriptor-driven stacks (from `Eln.sharedItem` and `SixNodeItem.orderList`) depend on those items having valid client models. Missing models stop them from rendering and make placement hard to verify.
- **OBJ-driven blocks are invisible without BE renderers**: `SixNodeBlock`/`TransparentNodeBlock` return `RenderShape.INVISIBLE` and expect `SixNodeRender`/`TransparentNodeRender` to draw OBJ meshes. If any descriptor misses its OBJ (`Eln.obj.getObj(...)`), the block becomes invisible. Check `Obj3DFolder` output in the log for failed loads from `assets/eln/model`.
- **Legacy lang files**: resources are still `.lang` (for example, `src/main/resources/assets/eln/lang/en_US.lang`). Forge 1.20.1 expects `assets/eln/lang/en_us.json`, so translation keys will show raw, which makes diagnosis harder.

## Likely Root Causes vs. Old Code
- The old renderer path (`UtilsClient.drawIcon` + `iconName` + `textures/items`) was not recreated in 1.20.1. Now, descriptor items are default-rendered and require data-driven models/textures per Forge 1.20.1’s model system (`Documentation-1.20.1/docs/resources/client/models/index.md`).
- Many textures live in `assets/eln/sprites` and are never referenced by models. In 1.7.10 these were manually bound in renderers; in 1.20.1, without JSON models or a custom item renderer, they are ignored.
- Some block models point to textures that no longer exist (`energy_converter`) or use placeholder `empty-texture` that provides no visual feedback, leading to “missing model” or invisible blocks.

## Remediation Plan
1) **Restore item icons**: either (a) add an item renderer for `GenericItemUsingDamageDescriptor` (similar to `ElnItemRenderer`) that reads `iconName` and draws a quad using textures under `textures/items`, or (b) add item JSON models (preferably via `ItemModelProvider` datagen per `Documentation-1.20.1/docs/datagen/client/modelproviders.md`) and port the item textures from `old_mod/.../textures/items`.
2) **Audit textures referenced by block/item models**: fix `energy_converter` (add or rename texture under `textures/block`), add blockstate/model/item-model triples for `ghost_block`, and verify all `blockstates/*.json` have valid parents/textures.
3) **Verify OBJ availability**: check the runtime log for `Obj3DFolder` “unable to load model” lines; ensure every descriptor used in `SixNodeRegistration`/`TransparentNodeRegistration` has a matching OBJ folder under `assets/eln/model`.
4) **Update translations**: convert key `.lang` files to `lang/en_us.json` (and others as needed) so translation keys resolve.
5) **Long-term**: move repeated manual assets to datagen (blockstates, block/item models, lang) to avoid drift from Forge 1.20.1 expectations.

***
