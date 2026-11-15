-- Los usuarios que entran con Google tienen acceso web y móvil (antes se creaban solo con acceso móvil).
-- Se activa la web a los que ya se crearon con Google.
update usuarios
set is_web_usuario = true
where id_usuario in (select id_usuario from cuentas_google);
