local name = I:getName(context.item)
global.create_mainhand_progress = 0.0;
global.create_offhand_progress = 0.0;
if not name:match("^create:") then
    if name ~= "minecraft:air" then
        return
    end
    local player = context.player
    if P:isClimbing(player) or P:isSwimming(player) or P:isCrawling(player) then
        return
    end
    local offName = I:getName(context.mainHand and P:getOffhandItem(player) or P:getMainItem(player))
    if offName == "create:linked_controller" then
        if not context.mainHand then
            M:translate(context.matrices, 0, -1, 0.5)
        end
    elseif (offName:match("^create:cardboard_package_") or offName:match("^create:rare_")) then
        M:translate(context.matrices, 0, -1, 0.5)
        registry:put("bowCount", 0)
    end
    return
end

if name == "create:linked_controller" then
    local player = context.player
    local create_progress = context.mainHand and create_mainhand_progress or create_offhand_progress
    if P:isUsingItem(player) and P:getActiveHand(player) == context.hand then
        create_progress = create_progress + 0.075 * context.deltaTime * 60
    else
        create_progress = create_progress - 0.075 * context.deltaTime * 60
    end
    create_progress = M:clamp(create_progress, 0, 1)
    if context.mainHand then
        create_mainhand_progress = create_progress
    else
        create_offhand_progress = create_progress
    end
    if create_progress > 0 and not P:isClimbing(player) and not P:isSwimming(player) and not P:isCrawling(player) then
        local mat = context.matrices
        local l = (context.bl and 1) or -1
        M:translate(mat, -0.2 * create_progress * l, -0.2 * create_progress, 0)
        M:rotateX(mat, 40 * create_progress, 0.3 * l, -0.4, 0)
        M:rotateY(mat, -10 * create_progress * l, 0.3 * l, -0.4, 0)
        M:rotateZ(mat, 20 * create_progress * l, 0.3 * l, -0.4, 0)
    end
elseif name == "create:wand_of_symmetry" then
    local create_progress = context.mainHand and create_mainhand_progress or create_offhand_progress
    if create_progress > 0 or (context.swingProgress < 0.5 and context.equipProgress == 0) then
        if context.swingProgress > 0 then
            create_progress = create_progress + 0.075 * context.deltaTime * 60
        else
            create_progress = create_progress - 0.075 * context.deltaTime * 60
        end
        create_progress = M:clamp(create_progress, 0, 1)
    end
    if context.mainHand then
        create_mainhand_progress = create_progress
    else
        create_offhand_progress = create_progress
    end
    if create_progress > 0 then
        local player = context.player
        if not P:isClimbing(player) and not P:isSwimming(player) and not P:isCrawling(player) then
            local mat = context.matrices
            local l = (context.bl and 1) or -1
            M:translate(mat, 0, 0, 0.1 * create_progress)
            M:rotateX(mat, 30 * create_progress, 0.3 * l, -0.4, 0)
            M:rotateY(mat, 20 * create_progress * l, 0.3 * l, -0.4, 0)
        end
    end
elseif name == "create:schedule" or name == "create:filter" or name == "create:attribute_filter" or name == "create:package_filter" then
    if context.mainHand then
        if create_mainhand_progress > 0 or (context.swingProgress < 0.5 and context.equipProgress == 0) then
            if context.swingProgress > 0 then
                create_mainhand_progress = create_mainhand_progress + 0.075 * context.deltaTime * 60
            else
                create_mainhand_progress = create_mainhand_progress - 0.075 * context.deltaTime * 30
            end
            create_mainhand_progress = M:clamp(create_mainhand_progress, 0, 1)
        end
        if create_mainhand_progress > 0 then
            local player = context.player
            if not P:isClimbing(player) and not P:isSwimming(player) and not P:isCrawling(player) then
                local mat = context.matrices
                local l = (context.bl and 1) or -1
                M:translate(mat, 0 * create_mainhand_progress, 0.4 * create_mainhand_progress, 0.3 * create_mainhand_progress)
                M:rotateX(mat, -20 * create_mainhand_progress, 0.3 * l, -0.4, 0)
                M:rotateY(mat, -10 * create_mainhand_progress * l, 0.3 * l, -0.4, 0)
            end
        end
    end
elseif I:isBlock(context.item) and name ~= "create:track" and name ~= "create:clipboard" and not name:match("_door$") then
    renderAsBlock:put(name, false)
elseif name:match("^create:cardboard_package_") or name:match("^create:rare_") then
    local player = context.player
    local create_progress = context.mainHand and create_mainhand_progress or create_offhand_progress
    if P:isUsingItem(player) and P:getActiveHand(player) == context.hand then
        create_progress = create_progress + 0.075 * context.deltaTime * 60
    else
        create_progress = create_progress - 0.075 * context.deltaTime * 60
    end
    create_progress = M:clamp(create_progress, 0, 1)
    if context.mainHand then
        create_mainhand_progress = create_progress
    else
        create_offhand_progress = create_progress
    end
    if create_progress > 0 and not P:isClimbing(player) and not P:isSwimming(player) and not P:isCrawling(player) then
        local mat = context.matrices
        local l = (context.bl and 1) or -1
        M:translate(mat, -0.2 * create_progress * l, -0.2 * create_progress, 0)
        M:rotateX(mat, 40 * create_progress, 0.3 * l, -0.4, 0)
        M:rotateY(mat, -10 * create_progress * l, 0.3 * l, -0.4, 0)
        M:rotateZ(mat, 20 * create_progress * l, 0.3 * l, -0.4, 0)
    end
end