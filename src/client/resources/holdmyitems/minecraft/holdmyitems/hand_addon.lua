local name = I:getName(item)
global.create_mainhand_progress = 0.0;
global.create_offhand_progress = 0.0;
if not name:match("^create:") then
    if name ~= "minecraft:air" then
        return
    end
    local player = player
    if P:isClimbing(player) or P:isSwimming(player) or P:isCrawling(player) then
        return
    end
    local offName = I:getName(mainHand and P:getOffhandItem(player) or P:getMainItem(player))
    if offName == "create:linked_controller" then
        if not mainHand then
            M:moveY(matrices, -1)
            M:moveZ(matrices, 0.5)
        end
    elseif (offName:match("^create:cardboard_package_") or offName:match("^create:rare_")) then
        M:moveY(matrices, -1)
        M:moveZ(matrices, 0.5)
        registry:put("bowCount", -1)
        registry:put("offhand", 1)
    end
    return
end

if name == "create:linked_controller" then
    local player = player
    local create_progress = mainHand and create_mainhand_progress or create_offhand_progress
    if P:isUsingItem(player) and P:getActiveHand(player) == hand then
        create_progress = create_progress + 0.075 * deltaTime * 60
    else
        create_progress = create_progress - 0.075 * deltaTime * 60
    end
    create_progress = M:clamp(create_progress, 0, 1)
    if mainHand then
        create_mainhand_progress = create_progress
    else
        create_offhand_progress = create_progress
    end
    if create_progress > 0 and not P:isClimbing(player) and not P:isSwimming(player) and not P:isCrawling(player) then
        local l = (bl and 1) or -1
        M:moveX(matrices, -0.2 * create_progress * l)
        M:moveY(matrices, -0.2 * create_progress)
        M:rotateX(matrices, 40 * create_progress, 0.3 * l, -0.4, 0)
        M:rotateY(matrices, -10 * create_progress * l, 0.3 * l, -0.4, 0)
        M:rotateZ(matrices, 20 * create_progress * l, 0.3 * l, -0.4, 0)
    end
elseif name == "create:wand_of_symmetry" then
    local create_progress = mainHand and create_mainhand_progress or create_offhand_progress
    if create_progress > 0 or (swingProgress < 0.5 and equipProgress == 0) then
        if swingProgress > 0 then
            create_progress = create_progress + 0.075 * deltaTime * 60
        else
            create_progress = create_progress - 0.075 * deltaTime * 60
        end
        create_progress = M:clamp(create_progress, 0, 1)
    end
    if mainHand then
        create_mainhand_progress = create_progress
    else
        create_offhand_progress = create_progress
    end
    if create_progress > 0 then
        local player = player
        if not P:isClimbing(player) and not P:isSwimming(player) and not P:isCrawling(player) then
            local l = (bl and 1) or -1
            M:moveZ(matrices, 0.1 * create_progress)
            M:rotateX(matrices, 30 * create_progress, 0.3 * l, -0.4, 0)
            M:rotateY(matrices, 20 * create_progress * l, 0.3 * l, -0.4, 0)
        end
    end
elseif name == "create:schedule" or name == "create:filter" or name == "create:attribute_filter" or name == "create:package_filter" then
    if mainHand then
        if create_mainhand_progress > 0 or (swingProgress < 0.5 and equipProgress == 0) then
            if swingProgress > 0 then
                create_mainhand_progress = create_mainhand_progress + 0.075 * deltaTime * 60
            else
                create_mainhand_progress = create_mainhand_progress - 0.075 * deltaTime * 30
            end
            create_mainhand_progress = M:clamp(create_mainhand_progress, 0, 1)
        end
        if create_mainhand_progress > 0 then
            local player = player
            if not P:isClimbing(player) and not P:isSwimming(player) and not P:isCrawling(player) then
                local l = (bl and 1) or -1
                M:moveY(matrices, 0.4 * create_mainhand_progress)
                M:moveZ(matrices, 0.3 * create_mainhand_progress)
                M:rotateX(matrices, -20 * create_mainhand_progress, 0.3 * l, -0.4, 0)
                M:rotateY(matrices, -10 * create_mainhand_progress * l, 0.3 * l, -0.4, 0)
            end
        end
    end
elseif I:isBlock(item) and name ~= "create:track" and name ~= "create:clipboard" and not name:match("_door$") then
    renderAsBlock:put(name, false)
elseif name:match("^create:cardboard_package_") or name:match("^create:rare_") then
    local player = player
    local create_progress = mainHand and create_mainhand_progress or create_offhand_progress
    if P:isUsingItem(player) and P:getActiveHand(player) == hand then
        create_progress = create_progress + 0.075 * deltaTime * 60
    else
        create_progress = create_progress - 0.075 * deltaTime * 60
    end
    create_progress = M:clamp(create_progress, 0, 1)
    if mainHand then
        create_mainhand_progress = create_progress
    else
        create_offhand_progress = create_progress
    end
    if create_progress > 0 and not P:isClimbing(player) and not P:isSwimming(player) and not P:isCrawling(player) then
        local l = (bl and 1) or -1
        M:moveX(matrices, -0.2 * create_progress * l)
        M:moveY(matrices, -0.2 * create_progress)
        M:rotateX(matrices, 40 * create_progress, 0.3 * l, -0.4, 0)
        M:rotateY(matrices, -10 * create_progress * l, 0.3 * l, -0.4, 0)
        M:rotateZ(matrices, 20 * create_progress * l, 0.3 * l, -0.4, 0)
    end
end