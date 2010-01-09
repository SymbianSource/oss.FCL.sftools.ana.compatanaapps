/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Symbian Foundation License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.symbianfoundation.org/legal/sfl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
*/

#include <variant/symbian_os_v9.3.hrh>
#include <e32def.h>
#include <e32base.h>
#include <s32strm.h>
#include <f32file.h>


#undef IMPORT_C
#define IMPORT_C __THIS_IS_EXPORTED__
#undef EXPORT_C
#define EXPORT_C __THIS_IS_EXPORTED__


//#ifndef __wchar_t
//#define __wchar_t wchar_t
//#endif


#ifndef _WCHAR_T_DECLARED
#define _WCHAR_T_DECLARED
#endif


typedef long n_long;
typedef short n_short;
typedef long n_time;

typedef int int32_t;


#ifndef __int64
#define __int64  long
#endif

