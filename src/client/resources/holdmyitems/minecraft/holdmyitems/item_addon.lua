local name = I:getName(item)
if not name:match("^create:") or name:match("_toolbox$") then
    return
end
function cancelDefaultBlockSwing()
    if registry:getOrDefault("create_block_swing", false) then
        return
    end
    local t = swingProgress
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

    M:rotateX(matrices, 5)
    M:moveZ(matrices, 0.025)
    M:moveY(matrices, 0.025)

    M:rotateX(matrices, 25 * swing_hit)
    M:rotateX(matrices, 10 * swing_rot)
    M:moveY(matrices, 0.05 * swing_rot)
    M:moveZ(matrices, 0.05 * swing_rot)
    M:rotateX(matrices, 10 * swing_hit)
    M:rotateX(matrices, 30 * swing_rot)
    M:rotateX(matrices, -10 * swing_rot)
    M:moveY(matrices, 0.05 * swing_rot)
    M:moveZ(matrices, 0.05 * swing_rot)

    M:moveY(matrices, -0.025)
    M:moveZ(matrices, -0.025)
    M:rotateX(matrices, -5)
end
function cancelDefaultItemSwing()
    if registry:getOrDefault("create_item_swing", false) then
        return
    end
    local t = swingProgress
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
    local ptAngle = registry:getOrDefault(mainHand and "pitchAngle" or "pitchAngleO", 0)

    local l = (bl and 1) or -1
    M:rotateZ(matrices, -6 * l)
    M:rotateY(matrices, 10 * l)
    M:rotateX(matrices, 8)
    M:moveX(matrices, 0.05 * l)

    M:rotateX(matrices, (P:getPitch(player) * 0.025) + ptAngle * -0.1, 0, -0.2, 0)
    M:rotateX(matrices, 20 * M:sin(equipProgress * equipProgress * equipProgress) + (ptAngle * -0.05), 0.3 * l, -0.4, 0)

    M:rotateX(matrices, 25 * swing_hit)
    M:rotateX(matrices, 10 * swing_rot)
    M:moveY(matrices, 0.05 * swing_rot)
    M:moveZ(matrices, 0.05 * swing_rot)
    M:rotateX(matrices, 10 * swing_hit)
    M:rotateX(matrices, 30 * swing_rot)
    M:rotateX(matrices, -10 * swing_rot)
    M:moveY(matrices, 0.05 * swing_rot)
    M:moveZ(matrices, 0.05 * swing_rot)

    M:moveX(matrices, -0.05 * l)
    M:rotateX(matrices, -8)
    M:rotateY(matrices, -10 * l)
    M:rotateZ(matrices, 6 * l)
end
function cancelCardboardItemSwing()
    if registry:getOrDefault("create_item_swing", false) then
        return
    end
    local t = swingProgress
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
    local ptAngle = registry:getOrDefault(mainHand and "pitchAngle" or "pitchAngleO", 0)

    local l = (bl and 1) or -1
    M:rotateX(matrices, (P:getPitch(player) * 0.025) + ptAngle * -0.1, 0, -0.2, 0)
    M:moveX(matrices, 0.065 * l)

    M:rotateX(matrices, 25 * swing_hit)
    M:rotateX(matrices, 10 * swing_rot)
    M:moveY(matrices, 0.05 * swing_rot)
    M:moveZ(matrices, 0.05 * swing_rot)
    M:rotateX(matrices, 10 * swing_hit)
    M:rotateX(matrices, 30 * swing_rot)
    M:rotateX(matrices, -10 * swing_rot)
    M:moveY(matrices, 0.05 * swing_rot)
    M:moveZ(matrices, 0.05 * swing_rot)

    M:moveX(matrices, -0.065 * l)
end
function applySwing()
    if registry:getOrDefault("create_item_swing", false) then
        return
    end
    local t = mainHand and mainHandSwingProgress or offHandSwingProgress
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
    M:moveZ(matrices, -0.05 * swing_rot)
    M:moveY(matrices, -0.05 * swing_rot)
    M:rotateX(matrices, 10 * swing_rot)
    M:rotateX(matrices, -30 * swing_rot)
    M:rotateX(matrices, -10 * swing_hit)
    M:moveZ(matrices, -0.05 * swing_rot)
    M:moveY(matrices, -0.05 * swing_rot)
    M:rotateX(matrices, -10 * swing_rot)
    M:rotateX(matrices, -25 * swing_hit)
    M:moveY(matrices, -0.025)
    M:moveZ(matrices, -0.025)
    M:rotateX(matrices, -5)
end

local l = (bl and 1) or -1
if name == "create:wrench" then
    M:moveZ(matrices, 0.1)
elseif name == "create:wand_of_symmetry" then
    cancelDefaultItemSwing()
    M:moveX(matrices, 0.14 * l)
    M:moveY(matrices, 0.1)
    M:moveZ(matrices, 0.08)
elseif name == "create:super_glue" or name == "create:minecart_coupling" then
    M:moveY(matrices, 0.06)
    M:moveZ(matrices, -0.3)
    if bl then
        M:moveX(matrices, -0.01)
        M:rotateZ(matrices, 10)
    else
        M:moveX(matrices, -0.15)
        M:rotateZ(matrices, -10)
    end
    M:rotateX(matrices, 80)
    M:rotateY(matrices, 100)
elseif name == "create:schedule" or name == "create:schematic_and_quill" then
    if mainHand then
        cancelDefaultItemSwing()
    end
    M:moveY(matrices, -0.12)
    if bl then
        M:moveX(matrices, -0.1)
        M:rotateZ(matrices, 20)
    else
        M:moveX(matrices, 0.28)
        M:rotateZ(matrices, 30)
        M:rotateY(matrices, -10)
    end
elseif name == "create:filter" or name == "create:attribute_filter" or name == "create:package_filter" then
    if mainHand then
        cancelDefaultItemSwing()
    end
    M:moveX(matrices, -0.3 * l)
    M:moveY(matrices, -0.2)
    M:rotateZ(matrices, -10 * l)
elseif name == "create:linked_controller" then
    cancelDefaultItemSwing()
    M:moveX(matrices, 0.08 * l)
    M:moveZ(matrices, 0.26)
    M:rotateZ(matrices, 0 * l)
    M:rotateX(matrices, -76)
    M:rotateY(matrices, 14 * l)
elseif I:isBlock(item) then
    if name == "create:track" then
        applySwing(mat)
        M:moveX(matrices, -0.16 * l)
        M:moveZ(matrices, 0.1)
        M:rotateZ(matrices, 103 * l)
        M:rotateX(matrices, 80)
        M:rotateY(matrices, 85 * l)
        M:scale(matrices, 0.88, 0.88, 0.88)
    elseif name == "create:clipboard" then
        M:moveX(matrices, -0.4 * l)
        M:moveZ(matrices, 0.2)
        M:rotateZ(matrices, 93 * l)
        M:rotateX(matrices, 90)
        M:rotateY(matrices, 85 * l)
    elseif name == "create:controller_rail" or name:match("_ladder$") or name:match("_bars$") or name:match("_pane$") then
        M:moveX(matrices, -0.1 * l)
        M:moveZ(matrices, 0.06)
        M:rotateZ(matrices, 8 * l)
        M:rotateX(matrices, -12)
        M:rotateY(matrices, -5 * l)
        M:scale(matrices, 0.88, 0.88, 0.88)
    elseif name:match("_door$") then
        M:moveX(matrices, 0.02 * l)
        M:moveY(matrices, 0.1)
        M:moveZ(matrices, 0.19)
        M:rotateZ(matrices, 5 * l)
        M:rotateY(matrices, 10 * l)
        M:scale(matrices, 0.88, 0.88, 0.88)
    else
        cancelDefaultBlockSwing()
        if name == "create:schematicannon" and not bl then
            M:moveY(matrices, 0.144)
            M:moveZ(matrices, -0.06)
            M:rotateZ(matrices, -83)
            M:rotateX(matrices, 73)
            M:rotateY(matrices, -93)
        elseif name == "create:belt_connector" then
            M:moveX(matrices, 0.04 * l)
            M:moveY(matrices, 0.1)
            M:moveZ(matrices, 0.06)
            M:rotateZ(matrices, -40 * l)
            M:rotateX(matrices, -160)
            M:rotateY(matrices, -90 * l)
        elseif name == "create:creative_motor" or ((name == "create:speedometer" or name == "create:stressometer") and bl) or ((name == "create:cuckoo_clock" or name:match("_postbox$")) and not bl) or name == "create:mechanical_drill" or name == "create:mechanical_saw" or name == "create:deployer" or name == "create:mechanical_harvester" then
            M:moveX(matrices, -0.2 * l)
            M:moveY(matrices, 0.156)
            M:moveZ(matrices, -0.02)
            M:rotateZ(matrices, 110 * l)
            M:rotateX(matrices, -226)
            M:rotateY(matrices, 76 * l)
        elseif name == "create:andesite_funnel" or name == "create:brass_funnel" then
            M:moveX(matrices, -0.22 * l)
            M:moveZ(matrices, 0.176)
            M:rotateZ(matrices, 67 * l)
            M:rotateX(matrices, -44)
            M:rotateY(matrices, 76 * l)
        elseif name == "create:copper_backtank" or name == "create:netherite_backtank" or name == "create:schematic_table" or name == "create:shaft" or name == "create:cogwheel" or name == "create:large_cogwheel" or name == "create:mechanical_press" or name == "create:chute" or name:match("_valve_handle") or name == "create:piston_extension_pole" or name == "create:display_board" or name == "create:nixie_tube" or name == "create:peculiar_bell" or name == "create:haunted_bell" or name == "create:desk_bell" then
            M:moveX(matrices, 0.04 * l)
            M:moveY(matrices, 0.152)
            M:moveZ(matrices, 0.176)
            M:rotateZ(matrices, 70 * l)
            M:rotateX(matrices, -44)
            M:rotateY(matrices, 76 * l)
        elseif name == "create:turntable" or name == "create:fluid_pipe" then
            M:moveX(matrices, -0.02 * l)
            M:moveY(matrices, 0.1)
            M:moveZ(matrices, 0.176)
            M:rotateZ(matrices, 70 * l)
            M:rotateX(matrices, -44)
            M:rotateY(matrices, 76 * l)
        elseif name == "create:hand_crank" or name == "create:sail_frame" or name == "create:white_sail" or name == "create:factory_gauge" or name == "create:analog_lever" or name == "create:placard" or name == "create:copycat_panel" or name == "create:train_trapdoor" or name == "create:framed_glass_trapdoor" then
            M:moveX(matrices, -0.02 * l)
            M:moveY(matrices, 0.05)
            M:moveZ(matrices, 0.176)
            M:rotateZ(matrices, 70 * l)
            M:rotateX(matrices, -44)
            M:rotateY(matrices, 76 * l)
        elseif name == "create:mechanical_roller" then
            M:moveX(matrices, -0.02 * l)
            M:moveY(matrices, 0.18)
            M:moveZ(matrices, 0.1)
            M:rotateZ(matrices, 74 * l)
            M:rotateY(matrices, 76 * l)
        elseif name == "create:andesite_tunnel" or name == "create:brass_tunnel" or name == "create:stock_link" or name == "create:display_link" then
            M:moveX(matrices, -0.02 * l)
            M:moveY(matrices, 0.20)
            M:moveZ(matrices, 0.176)
            M:rotateZ(matrices, 70 * l)
            M:rotateX(matrices, -44)
            M:rotateY(matrices, 76 * l)
        else
            M:moveX(matrices, -0.02 * l)
            M:moveY(matrices, 0.152)
            M:moveZ(matrices, 0.176)
            M:rotateZ(matrices, 70 * l)
            M:rotateX(matrices, -44)
            M:rotateY(matrices, 76 * l)
        end
        M:scale(matrices, 0.88, 0.88, 0.88)
    end
elseif name:match("^create:cardboard_package_") or name:match("^create:rare_") then
    cancelCardboardItemSwing()
    M:moveX(matrices, 0.018 * l)
    M:moveY(matrices, 0.086)
    M:moveZ(matrices, 0.172)
    M:rotateZ(matrices, 83 * l)
    M:rotateX(matrices, -21.5)
    M:rotateY(matrices, 93 * l)
end