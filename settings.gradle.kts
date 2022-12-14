/*****************************
 * Copyright (c) 2021 - 2023
 * author:yangxj96
 * email :yangxj96@gmail.com
 * date  :2023-01-07 00:11:48
 * Copyright (c) 2021 - 2023
 ****************************/

rootProject.name = "yangxj96-saas-api"

include("yangxj96-bean")
include("yangxj96-common")

include("yangxj96-serve:yangxj96-serve-auth")
findProject(":yangxj96-serve:yangxj96-serve-auth")?.name = "yangxj96-serve-auth"

include("yangxj96-serve:yangxj96-serve-gateway")
findProject(":yangxj96-serve:yangxj96-serve-gateway")?.name = "yangxj96-serve-gateway"

include("yangxj96-serve:yangxj96-serve-dept")
findProject(":yangxj96-serve:yangxj96-serve-dept")?.name = "yangxj96-serve-dept"

include("yangxj96-serve:yangxj96-serve-system")
findProject(":yangxj96-serve:yangxj96-serve-system")?.name = "yangxj96-serve-system"

include("yangxj96-starter:yangxj96-starter-remote")
findProject(":yangxj96-starter:yangxj96-starter-remote")?.name = "yangxj96-starter-remote"

include("yangxj96-starter:yangxj96-starter-security")
findProject(":yangxj96-starter:yangxj96-starter-security")?.name = "yangxj96-starter-security"

include("yangxj96-starter:yangxj96-starter-db")
findProject(":yangxj96-starter:yangxj96-starter-db")?.name = "yangxj96-starter-db"

include("yangxj96-starter:yangxj96-starter-common")
findProject(":yangxj96-starter:yangxj96-starter-common")?.name = "yangxj96-starter-common"
