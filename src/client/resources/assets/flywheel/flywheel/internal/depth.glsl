float linearize_depth(float d, float zNear, float zFar) {
    return (zNear * zFar) / (zNear + d * (zFar - zNear));
}

float delinearize_depth(float linearDepth, float zNear, float zFar) {
    return (zNear * (zFar - linearDepth)) / (linearDepth  * (zFar - zNear));
}