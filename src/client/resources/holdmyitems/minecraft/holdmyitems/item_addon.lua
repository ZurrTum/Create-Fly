local name = I:getName(context.item)
if not name:match("^create:") or name:match("_toolbox$") then
    return
end
function cancelDefaultBlockSwing()
    if registry:getOrDefault("create_block_swing", false) then
        return
    end
    local t = context.swingProgress
    local t2 = t * t
    local t3 = t2 * t
    t = 3 * t * (1 - t) * (1 - t) * 0.44 + 3 * t2 * (1 - t) * 0.94 + t3
    local swing_hit = M:sin(M:clamp(t, 0.16561, 0.49422) * 4.78 * 2 + 4.7)
    local swing_rot
    if t < 0.70016 then
        swing_rot = M:sin(M:clamp(t, 0, 0.308) * 5.1)
    else
        swing_rot = M:sin(M:clamp(t, 0.70016, 1) * 5.1 - 2)
    end
    swing_rot = swing_rot * swing_rot * swing_rot

    local mat = context.matrices
    M:rotateX(mat, 5)
    M:moveZ(mat, 0.025)
    M:moveY(mat, 0.025)

    M:rotateX(mat, 25 * swing_hit)
    M:rotateX(mat, 10 * swing_rot)
    M:moveY(mat, 0.05 * swing_rot)
    M:moveZ(mat, 0.05 * swing_rot)
    M:rotateX(mat, 10 * swing_hit)
    M:rotateX(mat, 30 * swing_rot)
    M:rotateX(mat, -10 * swing_rot)
    M:moveY(mat, 0.05 * swing_rot)
    M:moveZ(mat, 0.05 * swing_rot)

    M:moveY(mat, -0.025)
    M:moveZ(mat, -0.025)
    M:rotateX(mat, -5)
end
function cancelDefaultItemSwing()
    if registry:getOrDefault("create_item_swing", false) then
        return
    end
    local t = context.swingProgress
    local t2 = t * t
    local t3 = t2 * t
    t = 3 * t * (1 - t) * (1 - t) * 0.44 + 3 * t2 * (1 - t) * 0.94 + t3
    local swing_hit = M:sin(M:clamp(t, 0.16561, 0.49422) * 4.78 * 2 + 4.7)
    local swing_rot
    if t < 0.70016 then
        swing_rot = M:sin(M:clamp(t, 0, 0.308) * 5.1)
    else
        swing_rot = M:sin(M:clamp(t, 0.70016, 1) * 5.1 - 2)
    end
    swing_rot = swing_rot * swing_rot * swing_rot
    local ptAngle = registry:getOrDefault(context.mainHand and "pitchAngle" or "pitchAngleO", 0)

    local mat = context.matrices
    local l = (context.bl and 1) or -1
    M:rotateZ(mat, -6 * l)
    M:rotateY(mat, 10 * l)
    M:rotateX(mat, 8)
    M:moveX(mat, 0.05 * l)

    M:rotateX(mat, (P:getPitch(context.player) * 0.025) + ptAngle * -0.1, 0, -0.2, 0)
    M:rotateX(mat, 20 * M:sin(context.equipProgress * context.equipProgress * context.equipProgress) + (ptAngle * -0.05), 0.3 * l, -0.4, 0)

    M:rotateX(mat, 25 * swing_hit)
    M:rotateX(mat, 10 * swing_rot)
    M:moveY(mat, 0.05 * swing_rot)
    M:moveZ(mat, 0.05 * swing_rot)
    M:rotateX(mat, 10 * swing_hit)
    M:rotateX(mat, 30 * swing_rot)
    M:rotateX(mat, -10 * swing_rot)
    M:moveY(mat, 0.05 * swing_rot)
    M:moveZ(mat, 0.05 * swing_rot)

    M:moveX(mat, -0.05 * l)
    M:rotateX(mat, -8)
    M:rotateY(mat, -10 * l)
    M:rotateZ(mat, 6 * l)
end
function cancelCardboardItemSwing()
    if registry:getOrDefault("create_item_swing", false) then
        return
    end
    local t = context.swingProgress
    local t2 = t * t
    local t3 = t2 * t
    t = 3 * t * (1 - t) * (1 - t) * 0.44 + 3 * t2 * (1 - t) * 0.94 + t3
    local swing_hit = M:sin(M:clamp(t, 0.16561, 0.49422) * 4.78 * 2 + 4.7)
    local swing_rot
    if t < 0.70016 then
        swing_rot = M:sin(M:clamp(t, 0, 0.308) * 5.1)
    else
        swing_rot = M:sin(M:clamp(t, 0.70016, 1) * 5.1 - 2)
    end
    swing_rot = swing_rot * swing_rot * swing_rot
    local ptAngle = registry:getOrDefault(context.mainHand and "pitchAngle" or "pitchAngleO", 0)

    local mat = context.matrices
    local l = (context.bl and 1) or -1
    M:rotateX(mat, (P:getPitch(context.player) * 0.025) + ptAngle * -0.1, 0, -0.2, 0)
    M:moveX(mat, 0.065 * l)

    M:rotateX(mat, 25 * swing_hit)
    M:rotateX(mat, 10 * swing_rot)
    M:moveY(mat, 0.05 * swing_rot)
    M:moveZ(mat, 0.05 * swing_rot)
    M:rotateX(mat, 10 * swing_hit)
    M:rotateX(mat, 30 * swing_rot)
    M:rotateX(mat, -10 * swing_rot)
    M:moveY(mat, 0.05 * swing_rot)
    M:moveZ(mat, 0.05 * swing_rot)

    M:moveX(mat, -0.065 * l)
end
function applySwing()
    if registry:getOrDefault("create_item_swing", false) then
        return
    end
    local t = context.mainHand and context.mainHandSwingProgress or context.offHandSwingProgress
    local t2 = t * t
    local t3 = t2 * t
    t = 3 * t * (1 - t) * (1 - t) * 0.44 + 3 * t2 * (1 - t) * 0.94 + t3
    local swing_hit = M:sin(M:clamp(t, 0.16561, 0.49422) * 4.78 * 2 + 4.7)
    local swing_rot
    if t < 0.70016 then
        swing_rot = M:sin(M:clamp(t, 0, 0.308) * 5.1)
    else
        swing_rot = M:sin(M:clamp(t, 0.70016, 1) * 5.1 - 2)
    end
    swing_rot = swing_rot * swing_rot * swing_rot
    local mat = context.matrices
    M:moveZ(mat, -0.05 * swing_rot)
    M:moveY(mat, -0.05 * swing_rot)
    M:rotateX(mat, 10 * swing_rot)
    M:rotateX(mat, -30 * swing_rot)
    M:rotateX(mat, -10 * swing_hit)
    M:moveZ(mat, -0.05 * swing_rot)
    M:moveY(mat, -0.05 * swing_rot)
    M:rotateX(mat, -10 * swing_rot)
    M:rotateX(mat, -25 * swing_hit)
    M:moveY(mat, -0.025)
    M:moveZ(mat, -0.025)
    M:rotateX(mat, -5)
end

local mat = context.matrices
local l = (context.bl and 1) or -1
if name == "create:wrench" then
    M:translate(mat, 0, 0, 0.1)
elseif name == "create:wand_of_symmetry" then
    cancelDefaultItemSwing()
    M:translate(mat, 0.14 * l, 0.1, 0.08)
elseif name == "create:super_glue" or name == "create:minecart_coupling" then
    if context.bl then
        M:translate(mat, -0.01, 0.06, -0.3)
        M:rotateZ(mat, 10)
    else
        M:translate(mat, -0.15, 0.06, -0.3)
        M:rotateZ(mat, -10)
    end
    M:rotateX(mat, 80)
    M:rotateY(mat, 100)
elseif name == "create:schedule" or name == "create:schematic_and_quill" then
    if context.mainHand then
        cancelDefaultItemSwing()
    end
    if context.bl then
        M:translate(mat, -0.1, -0.12, 0)
        M:rotateZ(mat, 20)
    else
        M:translate(mat, 0.28, -0.12, 0)
        M:rotateZ(mat, 30)
        M:rotateY(mat, -10)
    end
elseif name == "create:filter" or name == "create:attribute_filter" or name == "create:package_filter" then
    if context.mainHand then
        cancelDefaultItemSwing()
    end
    M:translate(mat, -0.3 * l, -0.2, 0)
    M:rotateZ(mat, -10 * l)
elseif name == "create:linked_controller" then
    cancelDefaultItemSwing()
    M:translate(mat, 0.08 * l, 0, 0.26)
    M:rotateZ(mat, 0 * l)
    M:rotateX(mat, -76)
    M:rotateY(mat, 14 * l)
elseif name == "create:honey_bucket" or name == "create:chocolate_bucket" then
    M:moveY(mat, 0.025)
    M:moveX(mat, -0 * l)
    M:moveZ(mat, -0.1)
    M:rotateY(mat, 180)
    M:rotateX(mat, -82.5)
    M:rotateZ(mat, -20 * l)
elseif I:isBlock(context.item) then
    if name == "create:track" then
        applySwing(mat)
        M:translate(mat, -0.16 * l, 0.14, 0.1)
        M:rotateZ(mat, 83 * l)
        M:rotateX(mat, 70)
        M:rotateY(mat, 85 * l)
        M:scale(mat, 0.88, 0.88, 0.88)
    elseif name == "create:clipboard" then
        M:translate(mat, -0.4 * l, 0.1, 0.1)
        M:rotateZ(mat, 83 * l)
        M:rotateX(mat, 70)
        M:rotateY(mat, 85 * l)
    elseif name == "create:controller_rail" or name:match("_ladder$") or name:match("_bars$") or name:match("_pane$") then
        M:translate(mat, -0.1 * l, 0, 0.06)
        M:rotateZ(mat, 8 * l)
        M:rotateX(mat, -12)
        M:rotateY(mat, -5 * l)
        M:scale(mat, 0.88, 0.88, 0.88)
    elseif name:match("_door$") then
        M:translate(mat, 0.02 * l, 0.1, 0.19)
        M:rotateZ(mat, 5 * l)
        M:rotateY(mat, 10 * l)
        M:scale(mat, 0.88, 0.88, 0.88)
    else
        cancelDefaultBlockSwing()
        if name == "create:schematicannon" and not context.bl then
            M:translate(mat, 0, 0.086, 0)
            M:rotateZ(mat, -83)
            M:rotateX(mat, 73)
            M:rotateY(mat, -93)
        elseif name == "create:belt_connector" then
            M:translate(mat, 0.04 * l, 0.1, 0.06)
            M:rotateZ(mat, -40 * l)
            M:rotateX(mat, -160)
            M:rotateY(mat, -90 * l)
        elseif name == "create:creative_motor" then
            M:translate(mat, -0.1 * l, 0.1, 0.2)
            M:rotateZ(mat, 50 * l)
            M:rotateX(mat, -110)
            M:rotateY(mat, 33 * l)
        elseif (name == "create:cuckoo_clock" or name:match("_postbox$")) and not context.bl then
            M:translate(mat, 0.12, 0.14, 0)
            M:rotateZ(mat, -93)
            M:rotateX(mat, 60)
            M:rotateY(mat, -100)
        elseif (name == "create:speedometer" or name == "create:stressometer") and context.bl then
            M:translate(mat, -0.08, 0.1, 0)
            M:rotateZ(mat, 100)
            M:rotateX(mat, 80)
            M:rotateY(mat, 93)
        elseif name == "create:steam_whistle" or name == "create:packager" or name == "create:repackager" or name == "create:haunted_bell" or name == "create:peculiar_bell" or name == "create:desk_bell" then
            M:translate(mat, 0.018 * l, 0.1, 0.13)
            M:rotateZ(mat, 83 * l)
            M:rotateX(mat, -21.5)
            M:rotateY(mat, 93 * l)
        elseif name == "create:mechanical_drill" or name == "create:mechanical_saw" or name == "create:deployer" or name == "create:mechanical_harvester" or name == "create:item_hatch" then
            M:translate(mat, -0.2 * l, 0.16, 0)
            M:rotateZ(mat, 50 * l)
            M:rotateX(mat, 85)
            M:rotateY(mat, 143 * l)
        elseif name == "create:mechanical_plough" or name == "create:display_board" then
            M:translate(mat, -0.06 * l, 0.1, 0.15)
            M:rotateZ(mat, 83 * l)
            M:rotateX(mat, -21.5)
            M:rotateY(mat, 93 * l)
        elseif name == "create:controls" or name == "create:andesite_funnel" or name == "create:brass_funnel" then
            M:translate(mat, -0.1 * l, 0.086, 0.172)
            M:rotateZ(mat, 83 * l)
            M:rotateX(mat, -21.5)
            M:rotateY(mat, 93 * l)
        elseif name == "create:nixie_tube" then
            M:translate(mat, -0.22 * l, 0.16, 0.09)
            M:rotateZ(mat, 110 * l)
            M:rotateX(mat, 120)
            M:rotateY(mat, 100 * l)
        elseif name == "create:redstone_link" or name == "create:pulse_repeater" or name == "create:pulse_extender" or name == "create:pulse_timer" or name == "create:powered_latch" then
            M:translate(mat, -0.06 * l, 0.16, 0.18)
            M:rotateZ(mat, 90 * l)
            M:rotateX(mat, -60)
            M:rotateY(mat, 80 * l)
        else
            M:translate(mat, 0.018 * l, 0.086, 0.172)
            M:rotateZ(mat, 83 * l)
            M:rotateX(mat, -21.5)
            M:rotateY(mat, 93 * l)
        end
        M:scale(mat, 0.88, 0.88, 0.88)
    end
elseif name:match("^create:cardboard_package_") or name:match("^create:rare_") then
    cancelCardboardItemSwing()
    M:translate(mat, 0.018 * l, 0.086, 0.172)
    M:rotateZ(mat, 83 * l)
    M:rotateX(mat, -21.5)
    M:rotateY(mat, 93 * l)
end