/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.meta.service;

import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFilter;
import com.axelor.meta.db.repo.MetaFilterRepository;
import com.google.common.base.Objects;
import com.google.inject.persist.Transactional;
import java.util.List;
import javax.inject.Inject;
import org.apache.shiro.authz.AuthorizationException;

public class MetaFilterService {

  @Inject private MetaFilterRepository filters;

  @Transactional
  public MetaFilter saveFilter(MetaFilter ctx) {
    User user = AuthUtils.getUser();
    String query =
        "self.name = ?1 AND self.filterView = ?2 AND (self.user.code = ?3 OR self.shared = true)";
    MetaFilter filter =
        filters.all().filter(query, ctx.getName(), ctx.getFilterView(), user.getCode()).fetchOne();

    if (filter == null) {
      filter = new MetaFilter();
      filter.setName(ctx.getName());
      filter.setUser(user);
      filter.setFilterView(ctx.getFilterView());
    }

    filter.setTitle(ctx.getTitle());
    filter.setFilters(ctx.getFilters());
    filter.setFilterCustom(ctx.getFilterCustom());

    if (Objects.equal(filter.getUser(), user)) {
      filter.setShared(ctx.getShared());
    }

    return filters.save(filter);
  }

  @Transactional
  public MetaFilter removeFilter(MetaFilter ctx) {
    User user = AuthUtils.getUser();
    String query =
        "self.name = ?1 AND self.filterView = ?2 AND (self.user.code = ?3 OR self.shared = true)";
    MetaFilter filter =
        filters.all().filter(query, ctx.getName(), ctx.getFilterView(), user.getCode()).fetchOne();

    if (filter != null) {
      if (!Objects.equal(filter.getUser(), user)) {
        throw new AuthorizationException(I18n.get("You are not allowed to remove this filter"));
      }

      filters.remove(filter);
    }

    return ctx;
  }

  public List<MetaFilter> getFilters(String filterView) {
    User user = AuthUtils.getUser();
    String query = "self.filterView = ?1 AND (self.user.code = ?2 OR self.shared = true)";
    return Query.of(MetaFilter.class).filter(query, filterView, user.getCode()).order("id").fetch();
  }
}
