local name = I:getName(data.item)
if name == "create:honey_bucket" or name == "create:chocolate_bucket" then
    local ywAngle = registry:getOrDefault(data.mainHand and "yawAngle" or "yawAngleO", 0)
    local ptAngle = registry:getOrDefault(data.mainHand and "pitchAngle" or "pitchAngleO", 0)
    local liquid_anim = registry:getOrDefault("liquid_anim", 1)
    animator:rotateX(0, 1, M:clamp(-ptAngle * 0.6, -5, 5) * liquid_anim, 0.5, 0.7, 0.5)
    animator:rotateZ(0, 1, M:clamp(ywAngle * 0.6, -5, 5) * liquid_anim, 0.5, 0.7, 0.5)
end